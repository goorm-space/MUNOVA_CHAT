import http from "k6/http";
import {check} from "k6";
import {getToken} from "./auth.js";

export const options = {
    scenarios: {
        rsocket_load: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                {duration: "10s", target: 3000},
                // {duration: "60s", target: 1000},
                // {duration: "30s", target: 0}
            ],
            gracefulStop: "10s"
        }
    }
};

export default function () {

    const vuIndex = __VU - 1;
    const {token, memberId} = getToken(vuIndex);
    const chatId = 29685;

    const headers = {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
    };

    // Step 1: 채팅방 생성
    const r = http.post(`http://localhost:8082/api/chat/group/${chatId}`, null, {headers});
    check(r, {"chat created": (res) => res.status === 201});

    // Step 2: Node.js RSocket client 실행
    // exec.command("node", ["./rsocket-client.js", token]);
}