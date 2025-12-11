import {
    RSocketClient,
    JsonSerializer,
    IdentitySerializer,
    encodeRoute
} from "rsocket-core";
import RSocketWebSocketClient from "rsocket-websocket-client";
import { BufferEncoders } from "rsocket-core";

// CLI arguments: chatId, memberId
const chatId = Number(process.argv[2]);
const memberId = Number(process.argv[3]);

if (!chatId || !memberId) {
    console.error("Usage: node rsocket-client.js <chatId> <memberId>");
    process.exit(1);
}

const client = new RSocketClient({
    serializers: {
        data: JsonSerializer,
        metadata: IdentitySerializer,
    },
    setup: {
        keepAlive: 30000,
        lifetime: 90000,
        dataMimeType: "application/json",
        metadataMimeType: "message/x.rsocket.routing.v0",
    },
    transport: new RSocketWebSocketClient(
        { url: "ws://localhost:7070/rs" },
        BufferEncoders
    ),
});

console.log("🔵 Connecting RSocket", { chatId, memberId });

client.connect().subscribe({
    onComplete: (socket) => {
        console.log("🟢 RSocket Connected");

        // STEP 1) JOIN
        socket.requestResponse({
            metadata: encodeRoute("chat.join"),
            data: {
                type: "JOIN",
                chatId,
                senderId: memberId,
                timestamp: Date.now(),
            },
        }).subscribe({
            onComplete: () => console.log("JOIN OK"),
            onError: (e) => console.error("JOIN ERROR", e),
        });

        // STEP 2) STREAM SUBSCRIBE
        socket.requestStream({
            metadata: encodeRoute(`chat.stream.${chatId}`),
            data: null,
        }).subscribe({
            onSubscribe: (sub) => sub.request(1000),
            onNext: (payload) => {
                console.log("📩 STREAM >", payload.data);
            },
            onError: (err) => console.error("STREAM ERROR", err),
            onComplete: () => console.log("STREAM CLOSED"),
        });

        // STEP 3) 200ms 뒤 SEND
        setTimeout(() => {
            socket.fireAndForget({
                metadata: encodeRoute("chat.send"),
                data: {
                    type: "SEND",
                    chatId,
                    senderId: memberId,
                    content: "hello from RSocket load test",
                    timestamp: Date.now(),
                },
            });

            console.log("✉ SEND SENT");
        }, 200);

        // STEP 4) 1초 뒤 종료
        setTimeout(() => {
            console.log("⚫ Close RSocket");
            socket.close();
            process.exit(0);
        }, 1000);
    },

    onError: (err) => console.error("❌ RSocket Connection Failed:", err),
});