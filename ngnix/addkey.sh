chmod 600 xeon_vps1.pem
ssh-keygen -y -f xeon_vps1.pem > xeon_vps1.pub
cat xeon_vps1.pub | ssh ubuntu@165.154.235.179 "mkdir -p ~/.ssh && chmod 700 ~/.ssh && cat >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"


ssh athen@1.2.3.4
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys


ssh -i xeon_vps1.pem ubuntu@165.154.235.179
