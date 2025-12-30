'use client';

import React, { useState } from 'react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Message } from '@/services/chatService';
import { useAuthStore } from '@/stores/authStore';
import { Check, CheckCheck, Edit2, Trash2, X, Check as CheckIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
// Format time helper
const formatTimeAgo = (date: Date): string => {
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return 'vừa xong';
  if (diffMins < 60) return `${diffMins} phút trước`;
  if (diffHours < 24) return `${diffHours} giờ trước`;
  if (diffDays < 7) return `${diffDays} ngày trước`;
  return date.toLocaleDateString('vi-VN');
};

interface MessageItemProps {
  message: Message;
  showAvatar?: boolean;
  showName?: boolean;
  onEdit?: (message: Message) => void;
  onDelete?: (messageId: number) => void;
}

export function MessageItem({ 
  message, 
  showAvatar = true, 
  showName = false,
  onEdit,
  onDelete,
}: MessageItemProps) {
  const { user } = useAuthStore();
  const isOwnMessage = message.senderId === user?.id;
  const [isEditing, setIsEditing] = useState(false);
  const [editContent, setEditContent] = useState(message.content);
  const [showActions, setShowActions] = useState(false);

  // Format time
  const timeAgo = formatTimeAgo(new Date(message.createdAt));

  // Format content for deleted messages
  const displayContent = message.isDeleted 
    ? 'Tin nhắn đã được thu hồi' 
    : message.content;

  const handleEdit = () => {
    setIsEditing(true);
    setEditContent(message.content);
    setShowActions(false);
  };

  const handleSaveEdit = () => {
    if (editContent.trim() && editContent !== message.content) {
      onEdit?.({ ...message, content: editContent });
    }
    setIsEditing(false);
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
    setEditContent(message.content);
  };

  const handleDelete = () => {
    // Don't show confirm here - let parent handle it
    onDelete?.(message.id);
  };

  // Don't show actions for deleted messages
  if (message.isDeleted) {
    return (
      <div className={`flex w-full mb-2 ${isOwnMessage ? 'justify-end pr-0' : 'justify-start'}`}>
        {!isOwnMessage && <div className="flex-shrink-0 mr-2"><div className="h-8 w-8" /></div>}
        <div className={`flex flex-col ${isOwnMessage ? 'items-end' : 'items-start'} max-w-[75%]`}>
          <div className={`px-4 py-2.5 shadow-sm bg-muted/50 text-muted-foreground rounded-2xl italic opacity-60`}>
            <p className="text-sm">{displayContent}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={`flex w-full mb-2 ${isOwnMessage ? 'justify-end pr-0' : 'justify-start'}`}>
      {!isOwnMessage && (
        <div className="flex-shrink-0 mr-2">
          {showAvatar ? (
            <Avatar className="h-8 w-8">
          <AvatarImage src={message.senderAvatar || undefined} />
              <AvatarFallback className="text-xs">{message.senderName.charAt(0).toUpperCase()}</AvatarFallback>
        </Avatar>
          ) : (
            <div className="h-8 w-8" /> // Spacer when avatar is hidden
          )}
        </div>
      )}
      
      <div className={`flex flex-col ${isOwnMessage ? 'items-end' : 'items-start'} ${isOwnMessage ? 'max-w-[75%]' : 'max-w-[75%]'}`}>
        {showName && !isOwnMessage && (
          <span className="text-xs text-muted-foreground mb-1 px-1.5">{message.senderName}</span>
        )}
        
        <div
          className={`px-4 py-2.5 shadow-sm group relative ${
            isOwnMessage
              ? 'bg-primary text-primary-foreground rounded-2xl rounded-tr-none'
              : 'bg-muted text-foreground rounded-2xl rounded-tl-none'
          }`}
          onMouseEnter={() => isOwnMessage && setShowActions(true)}
          onMouseLeave={() => isOwnMessage && setShowActions(false)}
        >
          {isEditing ? (
            <div className="space-y-2">
              <Textarea
                value={editContent}
                onChange={(e) => setEditContent(e.target.value)}
                className="min-h-[60px] bg-background text-foreground"
                autoFocus
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && e.shiftKey) {
                    // Allow new line
                  } else if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    handleSaveEdit();
                  } else if (e.key === 'Escape') {
                    handleCancelEdit();
                  }
                }}
              />
              <div className="flex items-center gap-2 justify-end">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleCancelEdit}
                  className="h-7 px-2"
                >
                  <X className="h-3.5 w-3.5 mr-1" />
                  Hủy
                </Button>
                <Button
                  size="sm"
                  onClick={handleSaveEdit}
                  disabled={!editContent.trim() || editContent === message.content}
                  className="h-7 px-2"
                >
                  <CheckIcon className="h-3.5 w-3.5 mr-1" />
                  Lưu
                </Button>
              </div>
            </div>
          ) : (
            <>
              {/* Action buttons - only show for own messages on hover */}
              {isOwnMessage && showActions && (
                <div className="absolute -top-8 right-0 flex items-center gap-1 bg-background border rounded-lg p-1 shadow-md z-10">
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7"
                    onClick={handleEdit}
                    title="Sửa tin nhắn"
                  >
                    <Edit2 className="h-3.5 w-3.5" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7 text-destructive hover:text-destructive"
                    onClick={handleDelete}
                    title="Thu hồi tin nhắn"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </Button>
                </div>
              )}

          {message.messageType === 'IMAGE' && message.fileUrl && (
            <img
              src={message.fileUrl}
              alt="Image"
              className="max-w-xs rounded-lg mb-2 cursor-pointer"
              onClick={() => window.open(message.fileUrl!, '_blank')}
            />
          )}
          
          {message.messageType === 'FILE' && message.fileUrl && (
            <a
              href={message.fileUrl}
              download={message.fileName}
              className="flex items-center gap-2 text-sm underline"
            >
              📎 {message.fileName} ({(message.fileSize! / 1024).toFixed(1)} KB)
            </a>
          )}
          
          {message.messageType === 'TEXT' && (
            <p className="text-sm whitespace-pre-wrap break-words">{displayContent}</p>
          )}
          
          {message.isEdited && (
                <span className="text-xs opacity-70 mt-1 block">(đã chỉnh sửa)</span>
              )}
            </>
          )}
        </div>
        
        <div className={`flex items-center gap-1.5 mt-1 ${isOwnMessage ? 'flex-row-reverse' : 'flex-row'}`}>
          {isOwnMessage && (
            <span className={message.isRead ? 'text-blue-400' : 'text-muted-foreground'}>
              {message.isRead ? <CheckCheck className="h-3.5 w-3.5" /> : <Check className="h-3.5 w-3.5" />}
            </span>
          )}
          <span className="text-xs text-muted-foreground">{timeAgo}</span>
        </div>
      </div>
    </div>
  );
}

