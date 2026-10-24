# Infrastruktura – Azure VM pod Turbo Chińczyka

Terraform stawia jedną maszynę wirtualną w Azure, na której działa `docker compose` z projektem.

## Co powstaje

| Zasób | Nazwa | Koszt |
|---|---|---|
| Resource group | `rg-chinczyk` | – |
| VNet + subnet | `vnet-chinczyk` / `snet-chinczyk` | – |
| Network Security Group (firewall: 22, 80, 443) | `nsg-chinczyk` | – |
| Karta sieciowa | `nic-chinczyk` | – |
| Publiczny IP (statyczny, Standard) | `pip-chinczyk` | ~3,5–4 USD/mies. (zawsze) |
| VM `Standard_B2als_v2` (2 vCPU, 4 GB RAM), Ubuntu 24.04 | `vm-chinczyk` | ~0,04–0,05 USD/h (tylko gdy włączona) |
| Dysk systemowy 30 GB HDD | `vm-chinczyk_OsDisk_...` | ~1,5–2 USD/mies. (zawsze) |
| Auto-shutdown codziennie o 00:00 | – | – |

Region: `norwayeast`. Subskrypcja Azure for Students pozwala tylko na: `norwayeast`, `germanywestcentral`,
`francecentral`, `switzerlandnorth`, `spaincentral`.

Przy pierwszym starcie cloud-init (`terraform/cloud-init.yaml`) instaluje Dockera, compose, git i fail2ban
oraz utwardza SSH (brak logowania na roota, tylko klucze, `MaxAuthTries 5`).

## Kto co robi

> **Terraform uruchamia tylko jedna osoba (Jakub).** Stan infrastruktury (`terraform.tfstate`)
> jest trzymany lokalnie na jego komputerze i nie ma go w repo. Uruchomienie Terraform
> u kogoś innego spróbuje postawić wszystko od zera.

Pozostałe osoby potrzebują tylko: Azure CLI (do włączania VM) i klucza SSH.

---

## 1. Terraform (tylko właściciel state)

### Wymagania

- Terraform >= 1.6
- Azure CLI, zalogowane: `az login`
- Plik `terraform/terraform.tfvars` (nie jest w repo, wzór w `terraform.tfvars.example`):
  ```hcl
  subscription_id = "<id subskrypcji>"
  ssh_public_keys = {
    jakub = "ssh-ed25519 AAAA... jakub"
  }
  ```

### Postawienie / zmiany

```bash
cd infra/terraform
./apply.sh
```

Skrypt pokazuje plan, pyta o potwierdzenie, a potem robi `apply`. Azure na naszej subskrypcji potrafi
chwilowo zwracać 404 dla świeżo utworzonych zasobów (błędy `Provider produced inconsistent result after apply`,
`already exists - to be managed via Terraform`, `ResourceNotFound`). Skrypt w takiej sytuacji sam
importuje brakujące zasoby do state i ponawia `apply` (domyślnie do 5 razy, zmiana: `MAX_TRIES=8 ./apply.sh`).

Po zakończeniu:
```bash
terraform output    # m.in. vm_public_ip i ssh_command
```

### Usunięcie wszystkiego

```bash
terraform destroy
```

Nie usuwaj zasobów ręcznie w portalu – state rozjedzie się z Azure.

### Czego NIE zmieniać przez Terraform

Zmiana `cloud-init.yaml` lub `ssh_public_keys` wymusiłaby skasowanie i odtworzenie VM (razem z bazą).
Dlatego Terraform je ignoruje (`lifecycle.ignore_changes`). Nowe klucze dodajemy jak w punkcie 4.

---

## 2. Włączanie i wyłączanie VM

VM **wyłącza się sama codziennie o 00:00** (czas polski). **Włączenie jest ręczne.**
Publiczny IP jest statyczny – po ponownym włączeniu adres się nie zmienia.

```bash
# włącz
az vm start -g rg-chinczyk -n vm-chinczyk

# wyłącz (kończąc pracę wcześniej – oszczędza kredyt)
az vm deallocate -g rg-chinczyk -n vm-chinczyk

# status
az vm get-instance-view -g rg-chinczyk -n vm-chinczyk --query "instanceView.statuses[1].displayStatus" -o tsv
```

Albo w portalu: **Virtual machines → vm-chinczyk → Start / Stop**.

> `sudo shutdown` z wnętrza VM **nie** zatrzymuje naliczania opłat. Zawsze `deallocate` albo Stop w portalu.

Godzinę wyłączania zmienia się w `terraform/variables.tf` (`shutdown_time`, format `HHmm`).

### Dostęp dla reszty zespołu

Żeby ktoś mógł włączać/wyłączać VM, właściciel subskrypcji musi go zaprosić i nadać rolę:

```bash
az role assignment create \
  --assignee <email> \
  --role "Virtual Machine Contributor" \
  --scope /subscriptions/<subscription_id>/resourceGroups/rg-chinczyk
```

---

## 3. Logowanie przez SSH

Adres: `terraform output vm_public_ip` (lub w portalu przy `pip-chinczyk`). Użytkownik: `azureuser`.

```bash
ssh -i ~/.ssh/id_ed25519 -o IdentitiesOnly=yes azureuser@<IP>
```

Wygodniej – wpis w `~/.ssh/config`:
```
Host chinczyk
    HostName <IP>
    User azureuser
    IdentityFile ~/.ssh/id_ed25519
    IdentitiesOnly yes
```
i dalej tylko `ssh chinczyk`.

**Nie masz klucza?** `ssh-keygen -t ed25519`, a zawartość `~/.ssh/id_ed25519.pub` wyślij osobie z dostępem (punkt 4).

**Typowe problemy:**
- `Too many authentication failures` – brak `IdentitiesOnly yes`, SSH próbuje wszystkich kluczy z agenta.
- `UNPROTECTED PRIVATE KEY FILE` – `chmod 600 ~/.ssh/id_ed25519` (w WSL trzymaj klucz w `~/.ssh`, nie na `/mnt/c`).
- `Connection timed out` – VM jest wyłączona (punkt 2) albo twoje IP jest zbanowane (punkt 5).
- Diagnostyka: `ssh -v ...`.

---

## 4. Dodanie klucza SSH nowej osoby

Bez Terraform (nie odtwarza VM). Z komputera z Azure CLI:

```bash
az vm user update -g rg-chinczyk -n vm-chinczyk -u azureuser \
  --ssh-key-value "ssh-ed25519 AAAA... imie"
```

albo zalogowany na VM:
```bash
echo "ssh-ed25519 AAAA... imie" >> ~/.ssh/authorized_keys
```

Dopisz też klucz do `terraform.tfvars`, żeby był przy ewentualnym postawieniu VM od nowa.

---

## 5. fail2ban – odbanowanie IP

Po 5 nieudanych logowaniach w ciągu 10 minut IP jest banowane na 1 godzinę
(objawia się jako `Connection timed out` / `Connection refused`).

Sprawdź swoje IP: `curl ifconfig.me`

**Ktoś inny z zespołu jest zalogowany:**
```bash
sudo fail2ban-client status sshd                 # lista zbanowanych
sudo fail2ban-client set sshd unbanip <IP>
```

**Nikt nie ma dostępu** – przez Azure, bez SSH:
```bash
az vm run-command invoke -g rg-chinczyk -n vm-chinczyk \
  --command-id RunShellScript \
  --scripts "fail2ban-client set sshd unbanip <IP>"
```

Albo po prostu poczekaj godzinę.

---

## 6. Uruchomienie aplikacji na VM

```bash
git clone https://github.com/Yuuri2/Turbo-Chinczyk.git
cd Turbo-Chinczyk
nano .env          # co najmniej POSTGRES_PASSWORD=...
docker compose build backend && docker compose build frontend   # po kolei – VM ma 4 GB RAM i brak swapu
docker compose up -d
docker compose ps
```

Aplikacja: `http://<IP>/`.

---

## Znane problemy przy `terraform apply`

| Błąd | Przyczyna | Co zrobić |
|---|---|---|
| `RequestDisallowedByAzure` | region spoza listy dozwolonych dla Students | zmień `location` w `variables.tf` |
| `SkuNotAvailable` | brak wolnej pojemności dla rozmiaru VM w regionie | `vm_size = "Standard_B2s_v2"` albo inny region |
| `inconsistent result after apply`, `already exists`, `ResourceNotFound` (404) | opóźnienia API Azure | `./apply.sh` obsługuje to sam; ręcznie: `terraform import <adres> <id z błędu>` i ponowny apply |
| `terraform plan` trwa kilka minut | rejestracja providerów Azure | już wyłączone w `main.tf` (`resource_provider_registrations = "none"`) |
