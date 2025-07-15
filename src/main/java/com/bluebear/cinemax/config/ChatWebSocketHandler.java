//package com.bluebear.cinemax.config;
//
//import com.bluebear.cinemax.dto.ChatMessageDTO;
//import com.bluebear.cinemax.entity.ChatMessage;
//import com.bluebear.cinemax.service.chat.ChatService;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CopyOnWriteArraySet;
//
//@Component
//public class ChatWebSocketHandler extends TextWebSocketHandler {
//
//    @Autowired
//    private ChatService chatService;
//
//    // Key: session ID, Value: WebSocketSession object
//    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
//
//    // Key: room ID (e.g., "schedule-1", "support-123"), Value: Set of session IDs
//    private final Map<String, Set<String>> roomToSessionsMap = new ConcurrentHashMap<>();
//
//    // Key: session ID, Value: room ID
//    private final Map<String, String> sessionToRoomMap = new ConcurrentHashMap<>();
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) {
//        sessions.put(session.getId(), session);
//        System.out.println("New WebSocket connection: " + session.getId());
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//        Map<String, String> messageMap = objectMapper.readValue(payload, new TypeReference<>() {});
//        String messageType = messageMap.get("type");
//        String userId = messageMap.get("userId");
//
//        switch (messageType) {
//            case "JOIN_ROOM": {
//                String scheduleRoomId = "schedule-" + messageMap.get("roomId");
//                joinRoom(session, userId, scheduleRoomId);
//                break;
//            }
//            case "JOIN_SUPPORT": {
//                // Khi người dùng yêu cầu chat hỗ trợ, service sẽ tạo hoặc tìm phòng chat
//                // và trả về ID. ID này sẽ là định danh phòng.
//                Integer supportChatId = chatService.findOrCreateSupportChat(userId);
//                String supportRoomId = "support-" + supportChatId;
//                joinRoom(session, userId, supportRoomId);
//                break;
//            }
//            case "MESSAGE": {
//                String roomId = sessionToRoomMap.get(session.getId());
//                if (roomId != null) {
//                    // 1. Lưu tin nhắn vào DB
//                    ChatMessageDTO dto = objectMapper.readValue(payload, ChatMessageDTO.class);
//                    chatService.saveMessage(roomId, dto);
//
//                    // 2. Gửi tin nhắn đến mọi người trong phòng
//                    broadcastToRoom(roomId, payload, session.getId());
//                }
//                break;
//            }
//        }
//    }
//
//    private void joinRoom(WebSocketSession session, String userId, String roomId) throws IOException {
//        // Rời phòng cũ (nếu có)
//        leaveRoom(session);
//
//        // Tham gia phòng mới
//        roomToSessionsMap.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session.getId());
//        sessionToRoomMap.put(session.getId(), roomId);
//
//        // Tạo message thông báo user đã tham gia
//        Map<String, String> joinMessage = Map.of(
//                "type", "USER_JOINED",
//                "userId", userId
//        );
//        String joinMessagePayload = objectMapper.writeValueAsString(joinMessage);
//
//        broadcastToRoom(roomId, joinMessagePayload, session.getId());
//    }
//
//    private void leaveRoom(WebSocketSession session) throws IOException {
//        String roomId = sessionToRoomMap.get(session.getId());
//        if (roomId != null) {
//            Set<String> sessionsInRoom = roomToSessionsMap.get(roomId);
//            if (sessionsInRoom != null) {
//                sessionsInRoom.remove(session.getId());
//                if (sessionsInRoom.isEmpty()) {
//                    roomToSessionsMap.remove(roomId);
//                }
//            }
//            sessionToRoomMap.remove(session.getId());
//            System.out.printf("Session %s left room %s\n", session.getId(), roomId);
//        }
//    }
//
//    private void broadcastToRoom(String roomId, String message, String... excludeSessionIds) throws IOException {
//        Set<String> sessionsInRoom = roomToSessionsMap.get(roomId);
//        if (sessionsInRoom == null) return;
//
//        Set<String> excluded = Set.of(excludeSessionIds);
//
//        for (String sessionId : sessionsInRoom) {
//            if (!excluded.contains(sessionId)) {
//                WebSocketSession session = sessions.get(sessionId);
//                if (session != null && session.isOpen()) {
//                    session.sendMessage(new TextMessage(message));
//                }
//            }
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
//        leaveRoom(session);
//        sessions.remove(session.getId());
//    }
//}