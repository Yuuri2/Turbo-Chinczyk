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
  default = "polandcentral"
}

variable "vnet_cidr" {
  type    = string
  default = "10.42.0.0/27"
}

variable "subnet_cidr" {
  type    = string
  default = "10.42.0.0/27"
}

variable "ssh_publick_key" {
  description = "Klucz publiczny SSH"
  type        = string
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
  default = "Standard_B2s"
}