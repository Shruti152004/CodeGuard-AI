const express = require('express');
const http = require('http');
const WebSocket = require('ws');

const app = express();
const port = process.env.NODE_PORT || 3000;

app.get('/health', (req, res) => {
    res.json({ status: 'UP', service: 'Node.js Notification Service (Placeholder)' });
});

const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

wss.on('connection', (ws) => {
    console.log('Client connected to notification service');
    ws.send(JSON.stringify({ event: 'info', message: 'Connected to CodeGuard Notification service' }));
    
    ws.on('close', () => {
        console.log('Client disconnected');
    });
});

server.listen(port, () => {
    console.log(`Notification service listening on port ${port}`);
});
