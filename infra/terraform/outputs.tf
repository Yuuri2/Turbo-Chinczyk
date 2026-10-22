output "resource_group_name" {
  value = azurerm_resource_group.rg.name
}

output "subnet_id" {
  value = azurerm_subnet.subnet.id
}

output "vm_public_ip" {
  value = azurerm_public_ip.pip.ip_address
}