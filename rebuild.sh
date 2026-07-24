#!/bin/bash

set -e

sudo docker compose down -v --remove-orphans
sudo docker compose up -d --build
sudo docker compose ps -a