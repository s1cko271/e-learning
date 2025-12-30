'use client';

import React, { useEffect, useRef } from 'react';
import { MessageItem } from './MessageItem';
import { Message } from '@/services/chatService';
import { Loader2 } from 'lucide-react';

interface MessageListProps {
  messages: Message[];
  isLoading?: boolean;
  currentUserId?: number;
  onEditMessage?: (message: Message) => void;
  onDeleteMessage?: (messageId: number) => void;
}

export function MessageList({ 
  messages, 
  isLoading, 
  currentUserId,
  onEditMessage,
  onDeleteMessage,
}: MessageListProps) {
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const prevMessagesLengthRef = useRef<number>(0);

  // Auto scroll to bottom when new messages arrive
  useEffect(() => {
    if (messages.length > prevMessagesLengthRef.current) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
    prevMessagesLengthRef.current = messages.length;
  }, [messages.length]);

  // Group messages by date and ensure unique IDs
  const groupedMessages = React.useMemo(() => {
    // Filter out messages with invalid IDs and remove duplicates
    const validMessages = messages.filter((msg, index, self) => 
      msg.id != null && 
      self.findIndex(m => m.id === msg.id) === index
    );

    const groups: { date: string; messages: Message[] }[] = [];
    let currentDate = '';
    let currentGroup: Message[] = [];

    validMessages.forEach((message) => {
      const messageDate = new Date(message.createdAt).toLocaleDateString('vi-VN');
      
      if (messageDate !== currentDate) {
        if (currentGroup.length > 0) {
          groups.push({ date: currentDate, messages: currentGroup });
        }
        currentDate = messageDate;
        currentGroup = [message];
      } else {
        currentGroup.push(message);
      }
    });

    if (currentGroup.length > 0) {
      groups.push({ date: currentDate, messages: currentGroup });
    }

    return groups;
  }, [messages]);

  if (isLoading && messages.length === 0) {
    return (
      <div className="flex items-center justify-center h-full">
        <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (messages.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-full text-center p-8">
        <div className="text-4xl mb-4">💭</div>
        <h3 className="text-lg font-semibold mb-2">Chưa có tin nhắn nào</h3>
        <p className="text-sm text-muted-foreground">Bắt đầu cuộc trò chuyện...</p>
      </div>
    );
  }

  return (
    <div className="flex-1 overflow-y-auto p-4 pr-2">
      {groupedMessages.map((group, groupIndex) => (
        <div key={`${group.date}-${groupIndex}`} className="mb-6">
          {/* Date separator */}
          <div className="flex items-center justify-center my-6">
            <div className="bg-muted/50 px-4 py-1.5 rounded-full text-xs text-muted-foreground backdrop-blur-sm">
              {group.date === new Date().toLocaleDateString('vi-VN') ? 'Hôm nay' : group.date}
            </div>
          </div>
          
          {/* Messages */}
          <div className="space-y-0.5">
            {group.messages.map((message, index) => {
              const prevMessage = index > 0 ? group.messages[index - 1] : null;
              const nextMessage = index < group.messages.length - 1 ? group.messages[index + 1] : null;
              
              // Show avatar if this is first message from this sender or previous message is from different sender
              const showAvatar = !prevMessage || prevMessage.senderId !== message.senderId;
              
              // Show name if showing avatar and not current user
              const showName = showAvatar && message.senderId !== currentUserId;
              
              // Reduce spacing if next message is from same sender
              const isLastFromSender = !nextMessage || nextMessage.senderId !== message.senderId;
              
              return (
                <div key={`msg-${message.id}-${index}`} className={isLastFromSender ? 'mb-3' : 'mb-0.5'}>
                <MessageItem
                  message={message}
                  showAvatar={showAvatar}
                  showName={showName}
                    onEdit={onEditMessage}
                    onDelete={onDeleteMessage}
                />
                </div>
              );
            })}
          </div>
        </div>
      ))}
      
      <div ref={messagesEndRef} />
    </div>
  );
}

