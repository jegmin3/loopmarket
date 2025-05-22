package com.loopmarket.domain.chat.service;

import java.util.List;

import com.loopmarket.domain.chat.entity.ChatMessage;
import com.loopmarket.domain.chat.entity.ChatRoom;

public interface ChatService {

    ChatRoom createOrGetRoom(Integer user1Id, Integer user2Id);

    ChatMessage saveMessage(ChatMessage message);

    List<ChatMessage> getMessages(Long roomId);

    void markAsRead(Long roomId, Integer receiverId);

    void exitRoom(Long roomId, Integer userId);

    List<ChatRoom> getRoomsByUser(Integer userId);
}
