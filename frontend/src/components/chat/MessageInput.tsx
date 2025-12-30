'use client';

import React, { useState, useRef, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Paperclip, Send, Smile, X, Image as ImageIcon, File } from 'lucide-react';
import { SendMessageRequest } from '@/services/chatService';
import apiClient from '@/lib/api';
import { useUIStore } from '@/stores/uiStore';

interface MessageInputProps {
  conversationId: number;
  onSend: (request: SendMessageRequest) => void;
  onTyping?: (isTyping: boolean) => void;
  disabled?: boolean;
}

export function MessageInput({ conversationId, onSend, onTyping, disabled }: MessageInputProps) {
  const [content, setContent] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [uploadingFile, setUploadingFile] = useState(false);
  const [attachedFile, setAttachedFile] = useState<{
    fileUrl: string;
    fileName: string;
    fileSize: number;
    messageType: 'IMAGE' | 'FILE';
  } | null>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const typingTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const { addToast } = useUIStore();

  // Auto-resize textarea
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${Math.min(textareaRef.current.scrollHeight, 120)}px`;
    }
  }, [content]);

  const handleInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setContent(e.target.value);
    
    // Handle typing indicator
    if (!isTyping && e.target.value.trim()) {
      setIsTyping(true);
      onTyping?.(true);
    }
    
    // Clear existing timeout
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }
    
    // Set new timeout to stop typing indicator
    typingTimeoutRef.current = setTimeout(() => {
      setIsTyping(false);
      onTyping?.(false);
    }, 2000);
  };

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validate file size (max 10MB)
    if (file.size > 10 * 1024 * 1024) {
      addToast({
        type: 'error',
        description: 'File size không được vượt quá 10MB',
      });
      return;
    }

    setUploadingFile(true);
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await apiClient.post('/v1/chat/files/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      const { fileUrl, fileName, fileSize, messageType } = response.data;
      setAttachedFile({
        fileUrl,
        fileName,
        fileSize,
        messageType: messageType as 'IMAGE' | 'FILE',
      });
    } catch (error: any) {
      addToast({
        type: 'error',
        description: 'Không thể upload file: ' + (error.response?.data?.message || error.message),
      });
    } finally {
      setUploadingFile(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleRemoveFile = () => {
    setAttachedFile(null);
  };

  const handleSend = () => {
    if ((!content.trim() && !attachedFile) || disabled) return;

    onSend({
      conversationId,
      content: content.trim() || (attachedFile?.messageType === 'IMAGE' ? '[Hình ảnh]' : '[File đính kèm]'),
      messageType: attachedFile?.messageType || 'TEXT',
      fileUrl: attachedFile?.fileUrl,
      fileName: attachedFile?.fileName,
      fileSize: attachedFile?.fileSize,
    });

    setContent('');
    setAttachedFile(null);
    setIsTyping(false);
    onTyping?.(false);
    
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="border-t bg-card p-4">
      {/* Attached file preview */}
      {attachedFile && (
        <div className="mb-2 p-2 bg-muted rounded-lg flex items-center gap-2">
          {attachedFile.messageType === 'IMAGE' ? (
            <ImageIcon className="h-4 w-4 text-muted-foreground" />
          ) : (
            <File className="h-4 w-4 text-muted-foreground" />
          )}
          <span className="text-sm flex-1 truncate">{attachedFile.fileName}</span>
          <Button
            variant="ghost"
            size="icon"
            className="h-6 w-6"
            onClick={handleRemoveFile}
          >
            <X className="h-3.5 w-3.5" />
          </Button>
        </div>
      )}

      <div className="flex items-end gap-2">
        <input
          ref={fileInputRef}
          type="file"
          className="hidden"
          accept="image/*,.pdf,.doc,.docx,.txt"
          onChange={handleFileSelect}
          disabled={disabled || uploadingFile}
        />
        <Button
          variant="ghost"
          size="icon"
          className="flex-shrink-0"
          disabled={disabled || uploadingFile}
          onClick={() => fileInputRef.current?.click()}
          title="Đính kèm file"
        >
          {uploadingFile ? (
            <div className="h-5 w-5 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          ) : (
          <Paperclip className="h-5 w-5" />
          )}
        </Button>
        
        <div className="flex-1 relative">
          <Textarea
            ref={textareaRef}
            value={content}
            onChange={handleInputChange}
            onKeyDown={handleKeyDown}
            placeholder={attachedFile ? "Thêm nội dung (tùy chọn)..." : "Nhập tin nhắn..."}
            className="min-h-[44px] max-h-[120px] resize-none pr-12"
            disabled={disabled || uploadingFile}
            rows={1}
          />
          <Button
            variant="ghost"
            size="icon"
            className="absolute right-2 bottom-2 h-8 w-8"
            disabled={disabled}
            title="Emoji (sắp có)"
          >
            <Smile className="h-4 w-4" />
          </Button>
        </div>
        
        <Button
          onClick={handleSend}
          disabled={(!content.trim() && !attachedFile) || disabled || uploadingFile}
          size="icon"
          className="flex-shrink-0"
        >
          <Send className="h-5 w-5" />
        </Button>
      </div>
      
      <p className="text-xs text-muted-foreground mt-2">
        Nhấn Enter để gửi, Shift + Enter để xuống dòng
      </p>
    </div>
  );
}

