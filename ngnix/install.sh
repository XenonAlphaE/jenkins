# Update package index
sudo apt update

# Install Nginx
sudo apt install -y nginx

# Enable and start Nginx with systemctl
sudo systemctl enable nginx
sudo systemctl start nginx

# Install Certbot and the Nginx plugin
sudo apt install -y certbot python3-certbot-nginx

# (Optional) check status
sudo systemctl status nginx
