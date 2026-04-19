#!/bin/bash
# bootstrap.sh
# Configura una instancia EC2 Amazon Linux 2023 desde cero para correr voting app
# Uso: bash bootstrap.sh

set -e  # Para si cualquier comando falla

echo "=== 1. Actualizando sistema ==="
sudo dnf update -y

echo "=== 2. Instalando Docker ==="
sudo dnf install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user
newgrp docker

echo "=== 3. Instalando Docker Compose ==="
DOCKER_COMPOSE_VERSION="v2.24.0"
sudo curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" \
  -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker compose version

echo "=== 4. Instalando Git ==="
sudo dnf install -y git

echo "=== 5. Clonando repositorio ==="
# Cambia esto por tu repo
REPO_URL="https://github.com/TU_USUARIO/voting.git"
APP_DIR="$HOME/voting"

if [ -d "$APP_DIR" ]; then
  echo "Repositorio ya existe, haciendo pull..."
  cd "$APP_DIR" && git pull origin main
else
  git clone "$REPO_URL" "$APP_DIR"
fi

echo "=== 6. Levantando la aplicación ==="
cd "$APP_DIR"
docker compose up --build -d

echo "=== 7. Instalando Nginx ==="
sudo dnf install -y nginx
sudo systemctl start nginx
sudo systemctl enable nginx

echo "=== 8. Instalando Certbot ==="
sudo dnf install -y certbot

echo "==="
echo "Bootstrap completado."
echo ""
echo "Pasos manuales que quedan:"
echo "  1. Obtener certificado SSL:"
echo "     sudo certbot certonly --standalone -d TU_DOMINIO.duckdns.org"
echo ""
echo "  2. Crear configuracion de Nginx:"
echo "     sudo nano /etc/nginx/conf.d/voting.conf"
echo ""
echo "  3. Reiniciar Nginx:"
echo "     sudo systemctl restart nginx"
echo ""
echo "  4. Abrir puertos en Security Group de EC2:"
echo "     - 22  (SSH)"
echo "     - 80  (HTTP)"
echo "     - 443 (HTTPS)"
echo "     - 8080 (Backend - opcional)"
echo "     - 3000 (Frontend - opcional)"
echo "==="