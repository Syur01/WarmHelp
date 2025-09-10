package com.warmhelp.app.controllers;

import com.warmhelp.app.dtos.auth.ChatRequest;
import com.warmhelp.app.dtosResponses.ChatResponse;
import com.warmhelp.app.exceptions.ChatAlreadyExistException;
import com.warmhelp.app.exceptions.ChatNotFoundException;
import com.warmhelp.app.exceptions.NoChatExistsInTheRepository;
import com.warmhelp.app.models.Chat;
import com.warmhelp.app.models.Message;
import com.warmhelp.app.models.User;
import com.warmhelp.app.repositories.ChatRepository;
import com.warmhelp.app.services.ChatService;
import com.warmhelp.app.services.UserServiceChat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "https://warmhelp-frontend.onrender.com")
@RestController
@RequestMapping("/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserServiceChat userService;

    // ✅ Crear un nuevo chat con lista de IDs de usuarios
    @PostMapping
    public ResponseEntity<Chat> createChat(@RequestBody ChatRequest request) {
        Chat chat = chatService.createChatWithUsers(request.getUserIds());
        return ResponseEntity.ok(chat);
    }

    // ✅ Obtener chats por ID de usuario
    @GetMapping("/user/id/{userId}")
    public ResponseEntity<List<Chat>> getChatsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getChatsByUser(userId));
    }

    // ✅ Obtener chats por username
    @GetMapping("/user/username/{username}")
    public ResponseEntity<List<Chat>> getChatsByUsername(@PathVariable String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Chat> chats = chatService.getChatsByUser(user);
        return ResponseEntity.ok(chats); // Puede ser lista vacía
    }

    // ✅ Obtener un chat por ID
    @GetMapping("/{chatId}")
    public ResponseEntity<Chat> getChatById(@PathVariable Long chatId) throws ChatNotFoundException {
        Chat chat = chatService.getById(chatId);
        return ResponseEntity.ok(chat);
    }

    // ✅ Obtener chat entre dos usuarios
    @PostMapping("/between")
    public ResponseEntity<Chat> getOrCreateChat(@RequestBody Map<String, String> users) {
        String username1 = users.get("user1");
        String username2 = users.get("user2");

        if (username1 == null || username2 == null) {
            return ResponseEntity.badRequest().build();
        }

        Chat chat = chatService.getOrCreateChat(username1, username2);
        return ResponseEntity.ok(chat);
    }

    // ✅ Agregar un mensaje a un chat
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<Chat> addMessageToChat(@PathVariable Long chatId, @RequestBody Message message) throws ChatNotFoundException {
        Chat updatedChat = chatService.addMessageToChat(message, chatId);
        return ResponseEntity.ok(updatedChat);
    }

    // ✅ Obtener mensajes de un chat
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<Message>> getMessagesFromChat(@PathVariable Long chatId) throws NoChatExistsInTheRepository {
        List<Message> messages = chatService.getAllMessagesInChat(chatId);
        return ResponseEntity.ok(messages);
    }
    @DeleteMapping("/{chatId}")
    public ResponseEntity<String> deleteChat(@PathVariable Long chatId) {
        try {
            chatService.deleteChatById(chatId);
            return ResponseEntity.ok("Chat eliminado correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
    @GetMapping("/response/user/username/{username}")
    public ResponseEntity<List<ChatResponse>> getChatResponsesByUsername(@PathVariable String username) throws ChatNotFoundException {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ChatNotFoundException("User not found"));
        List<Chat> chats = chatService.getChatsByUser(user);
        List<ChatResponse> responses = chats.stream().map(ChatResponse::new).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/available-users/{username}")
    public ResponseEntity<List<User>> getAvailableUsers(@PathVariable String username) {
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> users = userService.findAllUsers().stream()
                .filter(u -> !u.getUsername().equals(username))
                .toList();

        return ResponseEntity.ok(users);
    }

}
