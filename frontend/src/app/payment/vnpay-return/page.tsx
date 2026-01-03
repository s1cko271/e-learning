'use client';

import React, { useEffect, useState } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { CheckCircle2, XCircle, Loader2, ArrowLeft } from 'lucide-react';
import { ROUTES } from '@/lib/constants';
import { useUIStore } from '@/stores/uiStore';
import { useCartStore } from '@/stores/cartStore';

export default function VNPayReturnPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { addToast } = useUIStore();
  const { refreshCart } = useCartStore();
  
  const [isProcessing, setIsProcessing] = useState(true);
  const [result, setResult] = useState<{
    success: boolean;
    message: string;
    transactionCode?: string;
  } | null>(null);

  useEffect(() => {
    const processReturn = async () => {
      try {
        // Get all VNPay return parameters
        const params: Record<string, string> = {};
        searchParams.forEach((value, key) => {
          params[key] = value;
        });

        // Call backend to verify and process payment
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/v1/vnpay/return?` +
          new URLSearchParams(params).toString()
        );

        const data = await response.json();
        
        setResult({
          success: data.success || false,
          message: data.message || 'Không thể xác thực thanh toán',
          transactionCode: data.transactionCode,
        });

        if (data.success) {
          // Refresh cart if needed
          await refreshCart();
          
          addToast({
            type: 'success',
            message: 'Thanh toán thành công!',
            description: 'Khóa học đã được thêm vào tài khoản của bạn.',
          });
        } else {
          addToast({
            type: 'error',
            message: 'Thanh toán thất bại',
            description: data.message || 'Vui lòng thử lại.',
          });
        }
      } catch (error: any) {
        console.error('Error processing VNPay return:', error);
        setResult({
          success: false,
          message: 'Lỗi xử lý thanh toán: ' + (error.message || 'Unknown error'),
        });
        addToast({
          type: 'error',
          message: 'Lỗi xử lý thanh toán',
          description: error.message || 'Vui lòng thử lại sau.',
        });
      } finally {
        setIsProcessing(false);
      }
    };

    processReturn();
  }, [searchParams, router, addToast, refreshCart]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl font-bold">
            Kết quả thanh toán
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          {isProcessing ? (
            <div className="flex flex-col items-center justify-center py-8 space-y-4">
              <Loader2 className="h-12 w-12 animate-spin text-primary" />
              <p className="text-muted-foreground">Đang xử lý kết quả thanh toán...</p>
            </div>
          ) : result ? (
            <>
              {/* Success State */}
              {result.success && (
                <div className="space-y-4">
                  <div className="flex items-center justify-center">
                    <div className="rounded-full bg-green-100 dark:bg-green-900/20 p-4">
                      <CheckCircle2 className="h-12 w-12 text-green-600 dark:text-green-400" />
                    </div>
                  </div>
                  <div className="text-center space-y-2">
                    <h3 className="text-xl font-semibold text-green-600 dark:text-green-400">
                      Thanh toán thành công!
                    </h3>
                    <p className="text-muted-foreground">
                      {result.message}
                    </p>
                    {result.transactionCode && (
                      <p className="text-sm text-muted-foreground font-mono">
                        Mã giao dịch: {result.transactionCode}
                      </p>
                    )}
                  </div>
                  <div className="space-y-2 pt-4">
                    <Button
                      onClick={() => router.push(ROUTES.STUDENT.MY_COURSES)}
                      className="w-full"
                    >
                      Xem khóa học của tôi
                    </Button>
                    <Button
                      variant="outline"
                      onClick={() => router.push(ROUTES.COURSES)}
                      className="w-full"
                    >
                      Tiếp tục mua khóa học
                    </Button>
                  </div>
                </div>
              )}

              {/* Failed State */}
              {!result.success && (
                <div className="space-y-4">
                  <div className="flex items-center justify-center">
                    <div className="rounded-full bg-red-100 dark:bg-red-900/20 p-4">
                      <XCircle className="h-12 w-12 text-red-600 dark:text-red-400" />
                    </div>
                  </div>
                  <div className="text-center space-y-2">
                    <h3 className="text-xl font-semibold text-red-600 dark:text-red-400">
                      Thanh toán thất bại
                    </h3>
                    <p className="text-muted-foreground">
                      {result.message}
                    </p>
                  </div>
                  <div className="space-y-2 pt-4">
                    <Button
                      onClick={() => router.push(ROUTES.COURSES)}
                      className="w-full"
                    >
                      Quay lại danh sách khóa học
                    </Button>
                    <Button
                      variant="outline"
                      onClick={() => router.back()}
                      className="w-full"
                    >
                      <ArrowLeft className="mr-2 h-4 w-4" />
                      Quay lại
                    </Button>
                  </div>
                </div>
              )}
            </>
          ) : null}
        </CardContent>
      </Card>
    </div>
  );
}

