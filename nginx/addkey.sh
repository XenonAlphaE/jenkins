ssh-keygen -t rsa -b 4096 -m PEM -f xeon_vps1.pem

ssh ubuntu@165.154.235.179


chmod 600 xeon_vps1.pem
ssh-keygen -y -f xeon_vps1.pem > xeon_vps1.pub
cat xeon_vps1.pub | ssh ubuntu@165.154.235.179 "mkdir -p ~/.ssh && chmod 700 ~/.ssh && cat >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"


ssh athen@1.2.3.4
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys


ssh -i xeon_vps1.pem ubuntu@165.154.235.179



ssh-keygen -t rsa -b 4096 -m PEM -f ico_vps1.pem
chmod 600 ico_vps1.pem
ssh-keygen -y -f ico_vps1.pem > ico_vps1.pub
cat ico_vps1.pub | ssh root@193.57.33.177 "mkdir -p ~/.ssh && chmod 700 ~/.ssh && cat >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"


ssh root@193.57.33.177

ssh -i ico_vps1.pem root@193.57.33.177


ssh-keygen -t rsa -b 4096 -m PEM -f xeon_vps2.pem
chmod 600 xeon_vps2.pem
ssh-keygen -y -f xeon_vps2.pem > xeon_vps2.pub
cat xeon_vps2.pub | ssh root@104.219.233.183 "mkdir -p ~/.ssh && chmod 700 ~/.ssh && cat >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"

ssh athen@1.2.3.4
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys

ssh -i xeon_vps2.pem root@104.219.233.183
