FROM nodered/node-red:latest

COPY flows.json /data/flows.json
RUN npm install node-red-node-email

EXPOSE 1880
