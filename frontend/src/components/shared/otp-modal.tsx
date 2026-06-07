'use client';

import { useState, useRef, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Loader2 } from 'lucide-react';
import { motion } from 'framer-motion';

interface OTPModalProps {
  open: boolean;
  method: 'whatsapp' | 'email';
  onVerify: (otp: string) => Promise<void>;
  onResend: () => Promise<void>;
  onClose: () => void;
}

export function OTPModal({ open, method, onVerify, onResend, onClose }: OTPModalProps) {
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [error, setError] = useState('');
  const [isVerifying, setIsVerifying] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  useEffect(() => {
    if (open) {
      inputRefs.current[0]?.focus();
    }
  }, [open]);

  const handleChange = (index: number, value: string) => {
    if (!/^\d*$/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value.slice(-1);
    setOtp(newOtp);
    setError('');

    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    const newOtp = [...otp];
    
    pastedData.split('').forEach((char, i) => {
      if (i < 6) newOtp[i] = char;
    });
    
    setOtp(newOtp);
    const nextIndex = Math.min(pastedData.length, 5);
    inputRefs.current[nextIndex]?.focus();
  };

  const handleSubmit = async () => {
    const code = otp.join('');
    
    if (code.length !== 6) {
      setError('Please enter 6-digit code');
      return;
    }

    setIsVerifying(true);
    setError('');

    try {
      await onVerify(code);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Invalid OTP code');
      setOtp(['', '', '', '', '', '']);
      inputRefs.current[0]?.focus();
    } finally {
      setIsVerifying(false);
    }
  };

  const handleResend = async () => {
    setIsResending(true);
    try {
      await onResend();
      setOtp(['', '', '', '', '', '']);
      inputRefs.current[0]?.focus();
    } catch (err) {
      setError('Failed to resend OTP');
    } finally {
      setIsResending(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Verify OTP</DialogTitle>
          <DialogDescription>
            Enter the 6-digit code sent to your {method === 'whatsapp' ? 'WhatsApp' : 'email'}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          <div className="flex gap-2 justify-center" onPaste={handlePaste}>
            {otp.map((digit, index) => (
              <Input
                key={index}
                ref={(el) => { inputRefs.current[index] = el; }}
                type="text"
                inputMode="numeric"
                maxLength={1}
                value={digit}
                onChange={(e) => handleChange(index, e.target.value)}
                onKeyDown={(e) => handleKeyDown(index, e)}
                className="w-12 h-14 text-center text-xl font-bold"
                disabled={isVerifying}
              />
            ))}
          </div>

          {error && (
            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="text-sm text-red-500 text-center"
            >
              {error}
            </motion.p>
          )}

          <Button
            onClick={handleSubmit}
            disabled={isVerifying || otp.some(d => !d)}
            className="w-full"
          >
            {isVerifying ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Verifying...
              </>
            ) : (
              'Verify'
            )}
          </Button>

          <div className="text-center">
            <button
              type="button"
              onClick={handleResend}
              disabled={isResending}
              className="text-sm text-blue-600 hover:text-blue-700 disabled:opacity-50"
            >
              {isResending ? 'Sending...' : 'Resend code'}
            </button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
