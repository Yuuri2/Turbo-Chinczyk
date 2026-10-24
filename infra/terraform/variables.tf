variable "subscription_id" {
  description = "ID subskrypcji Azure"
  type        = string
}

variable "project" {
  type    = string
  default = "chinczyk"
}

variable "location" {
  type    = string
  default = "norwayeast"
}

variable "vnet_cidr" {
  type    = string
  default = "10.42.0.0/24"
}

variable "subnet_cidr" {
  type    = string
  default = "10.42.0.0/27"
}

variable "admin_username" {
  description = "Użytkownik admina na VM"
  type        = string
  default     = "azureuser"
}

variable "ssh_public_keys" {
  description = "Klucze publiczne SSH: nazwa osoby => zawartość klucza"
  type        = map(string)
}

variable "app_ports" {
  description = "Porty aplikacji otwarte z internetu"
  type        = list(string)
  default     = ["80", "443"]
}

variable "vm_size" {
  type    = string
  default = "Standard_B2als_v2" #Jeżeli będzie brakowało miejsca zmieniamy na Standard_B2s_v2
}

variable "shutdown_time" {
  description = "Godzina automatycznego wyłączenia VM (HHmm)"
  type        = string
  default     = "0000"

  validation {
    condition     = can(regex("^([01][0-9]|2[0-3])[0-5][0-9]$", var.shutdown_time))
    error_message = "shutdown_time musi być w formacie HHmm, np. 0000 (północ) lub 2300."
  }
}

variable "shutdown_timezone" {
  description = "Strefa czasowa harmonogramu (format Windows)"
  type        = string
  default     = "Central European Standard Time"
}
