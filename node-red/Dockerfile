FROM nodered/node-red:latest

COPY flows.json /data/flows.json
RUN npm install node-red-node-email
COPY settings.js /data/settings.js
COPY flows_cred.json /data/flows_cred.json

EXPOSE 1880
