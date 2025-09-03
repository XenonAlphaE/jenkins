# Update package index
sudo apt update

# Install Nginx
sudo apt install -y nginx

# Enable and start Nginx with systemctl
sudo systemctl enable nginx
sudo systemctl start nginx

# Install Certbot and the Nginx plugin
sudo apt install -y certbot python3-certbot-nginx


# install recomend options
sudo mkdir -p /etc/letsencrypt
sudo wget https://raw.githubusercontent.com/certbot/certbot/master/certbot-nginx/certbot_nginx/_internal/tls_configs/options-ssl-nginx.conf -O /etc/letsencrypt/options-ssl-nginx.conf
sudo wget https://raw.githubusercontent.com/certbot/certbot/master/certbot/certbot/ssl-dhparams.pem -O /etc/letsencrypt/ssl-dhparams.pem

# -----------------------------
# Install Docker Engine + Compose
# -----------------------------
sudo apt install -y docker.io docker-compose-v2

# -----------------------------
# Verify installation
# -----------------------------
docker --version
docker compose version


# (Optional) check status
sudo systemctl status nginx
