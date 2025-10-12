docker volume create jenkins_home
docker run -d --name jenkins-sandbox -p 12000:8080 -p 12001:50000 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts

docker exec jenkins-sandbox cat /var/jenkins_home/secrets/initialAdminPassword

jenkins-plugin-cli --plugins \
    git \
    github \
    github-branch-source \
    ssh-agent \
    publish-over-ssh



docker exec -it jenkins-sandbox bash
ssh -vT git@github.com


docker exec -it  -u  jenkins jenkins-sandbox bash
which npm
# or
echo $PATH


# fix the node-gyp
docker exec -it -u root jenkins-sandbox bash
sudo apt-get update && sudo apt-get install -y redis-tools

# Inside Jenkins container
apt-get update
apt-get install -y python3 build-essential
which python3

--reset container




check ssh key pass:
ssh-keygen -y -f /Users/steve/.ssh/id_ed25519_stevehuytrannd92


3. Remove the passphrase on the copy
ssh-keygen -p \
  -f keys/id_ed25519_stevehuytrannd92 \
  -N "" \
  -P "123456"



ssh ${vpsUser}@${vpsHost} 'ln -sf /etc/nginx/sites-available/${tmpConfigFile} /etc/nginx/sites-enabled/${tmpConfigFile}'
ssh ${vpsUser}@${vpsHost} 'nginx -t && systemctl reload nginx'


ssh -i /Users/steve/Coding/Jenkins/keys/Test123.pem ubuntu@165.154.235.205

ssh -i keys/Test123.pem root@165.154.235.205
ssh -i keys/xeon_vps1.pem ubuntu@165.154.235.179



sudo certbot certonly --webroot -w /var/www/presale/btcswifts -d btcswifts.com -d www.btcswifts.com


docker exec -it jenkins-sandbox bash

# list workspaces
ls -lh /var/jenkins_home/workspace

# remove old ones (careful!)
rm -rf /var/jenkins_home/workspace/build-web
rm -rf /var/jenkins_home/workspace/build-web@script
rm -rf /var/jenkins_home/workspace/build-web@tmp
rm -rf /var/jenkins_home/workspace/build-web
rm -rf /var/jenkins_home/workspace/build-web



# handle link ngnix:
ls -l /etc/nginx/sites-enabled/
sudo rm -r /etc/nginx/sites-enabled/sites-available
sudo nginx -t
sudo systemctl reload nginx


# dig installation
docker exec -it -u root jenkins-sandbox bash

apt-get update && apt-get install -y dnsutils
apt-get update && apt-get install -y rsync


redundant.each { cert ->
    sh """
        ssh -o StrictHostKeyChecking=no ${vpsInfo.vpsUser}@${vpsInfo.vpsHost} "
            sudo rm -rf /etc/letsencrypt/live/${cert} /etc/letsencrypt/archive/${cert} /etc/letsencrypt/renewal/${cert}.conf || true
        "
    """
}


======== docker compose =============
docker-compose up -d


# docker inside docker #
curl http://dind:2375/version
ping dind
echo $DOCKER_HOST


environment:
  - DOCKER_HOST=tcp://dind:2375


# certbot debug #
ssh -i keys/xeon_vps1.pem ubuntu@165.154.235.179


sudo mkdir -p /var/www/presale/supepe_com/.well-known/acme-challenge
echo "hello-test" | sudo tee /var/www/presale/supepe_com/.well-known/acme-challenge/test.txt
sudo chown -R www-data:www-data /var/www/presale/supepe_com
sudo nginx -t && sudo systemctl reload nginx

curl -v http://supepe.com/.well-known/acme-challenge/test.txt


sudo tail -f /var/log/nginx/access.log # ensure requst challange come to nginx server
<cmd certbot >
 sudo certbot certonly --webroot -w /var/www/presale/supepe_com -d supepe.com -d www.supepe.com -v  --agree-tos   --email contact@supepe.com  --non-interactive

 sudo certbot certonly --staging  --webroot -w /var/www/presale/supepe_com -d supepe.com -d www.supepe.com -v  --agree-tos   --email contact@supepe.com  --non-interactive


 ✅ Check letsdebug.net/supepe.com — it tells you which validation node failed.
 curl --data '{"method":"http-01","domain":"supepe.com"}' -H 'content-type: application/json' https://letsdebug.net
 curl -H 'accept: application/json' https://letsdebug.net/supepe.com/2584971



 # cloudfare config #
sudo mkdir -p /etc/ssl/cloudflare && sudo chown ubuntu:ubuntu /etc/ssl/cloudflare



scp -o StrictHostKeyChecking=no  /Users/steve/Coding/jenkins_xenon/supepe/supepe.com.crt ubuntu@165.154.235.179:/etc/ssl/cloudflare/supepe.com/supepe.com.crt
scp -o StrictHostKeyChecking=no  /Users/steve/Coding/jenkins_xenon/supepe/supepe.com.key ubuntu@165.154.235.179:/etc/ssl/cloudflare/supepe.com/supepe.com.key


echo "hello-test" | sudo tee /var/www/presale/supepe_com/index.html

sudo chmod 600 /etc/ssl/cloudflare/supepe.com/supepe.com.crt
sudo chmod 600 /etc/ssl/cloudflare/supepe.com/supepe.com.key


scp -o StrictHostKeyChecking=no /Users/steve/Coding/jenkins_xenon/supepe/supepe_com.conf ubuntu@165.154.235.179:/home/ubuntu/supepe_com.conf


sudo mv /home/ubuntu/supepe_com.conf /etc/nginx/sites-available/supepe_com.conf &&
sudo chown root:root /etc/nginx/sites-available/supepe_com.conf &&
sudo ln -sf /etc/nginx/sites-available/supepe_com.conf /etc/nginx/sites-enabled/supepe_com.conf


sudo nginx -t -v &&
sudo systemctl reload nginx

sudo grep -R "server_name" /etc/nginx/sites-enabled /etc/nginx/conf.d /etc/nginx/sites-available | grep -E "supepe\.com"
sudo nginx -T | grep "server_name"
sudo nginx -T | grep -B2 "server_name" | grep supepe.com

