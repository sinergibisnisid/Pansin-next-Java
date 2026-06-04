'use client';

import { useState, useRef, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Shield, KeyRound } from 'lucide-react';
import { motion } from 'framer-motion';
import { usePinGateStore } from '@/stores';
import { pinGateService } from '@/services';

export default function PinGatePage() {
  const router = useRouter();
  const { isVerified, setVerified } = usePinGateStore();
  const [pin, setPin] = useState(['', '', '', '', '', '']);
  const [error, setError] = useState('');
  const [isVerifying, setIsVerifying] = useState(false);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  // If already verified, redirect to landing
  useEffect(() => {
    if (isVerified) {
      router.replace('/');
    }
  }, [isVerified, router]);

  useEffect(() => {
    inputRefs.current[0]?.focus();
  }, []);

  const handleChange = (index: number, value: string) => {
    if (!/^\d*$/.test(value)) return;

    const newPin = [...pin];
    newPin[index] = value.slice(-1);
    setPin(newPin);
    setError('');

    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (e.key === 'Backspace' && !pin[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    const newPin = [...pin];
    
    pastedData.split('').forEach((char, i) => {
      if (i < 6) newPin[i] = char;
    });
    
    setPin(newPin);
    const nextIndex = Math.min(pastedData.length, 5);
    inputRefs.current[nextIndex]?.focus();
  };

  const handleSubmit = async () => {
    const code = pin.join('');
    
    if (code.length !== 6) {
      setError('Masukkan 6-digit PIN');
      return;
    }

    setIsVerifying(true);
    setError('');

    try {
      const isValid = await pinGateService.verifyPin(code);
      
      if (isValid) {
        setVerified(true);
        router.push('/');
      } else {
        setError('PIN tidak valid');
        setPin(['', '', '', '', '', '']);
        inputRefs.current[0]?.focus();
      }
    } catch {
      setError('Verifikasi gagal');
    } finally {
      setIsVerifying(false);
    }
  };

  // Don't render if already verified (waiting for redirect)
  if (isVerified) return null;

  return (
    <div className="relative min-h-screen flex items-center justify-center overflow-hidden bg-[#0a0e1a]">
      {/* Animated Background */}
      <div className="absolute inset-0">
        <motion.div
          animate={{ x: [0, 80, 0], y: [0, -40, 0], scale: [1, 1.2, 1] }}
          transition={{ duration: 20, repeat: Infinity, ease: 'linear' }}
          className="absolute -top-40 -right-40 h-80 w-80 rounded-full bg-blue-600/20 blur-[100px]"
        />
        <motion.div
          animate={{ x: [0, -60, 0], y: [0, 50, 0], scale: [1, 1.3, 1] }}
          transition={{ duration: 25, repeat: Infinity, ease: 'linear' }}
          className="absolute -bottom-40 -left-40 h-96 w-96 rounded-full bg-cyan-500/15 blur-[120px]"
        />
        <div
          className="absolute inset-0 opacity-[0.03]"
          style={{
            backgroundImage: `linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px)`,
            backgroundSize: '60px 60px',
          }}
        />
      </div>

      {/* PIN Card */}
      <motion.div
        initial={{ opacity: 0, y: 20, scale: 0.95 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        transition={{ duration: 0.5, ease: 'easeOut' }}
        className="relative z-10 w-full max-w-md mx-4"
      >
        <div className="rounded-2xl border border-white/10 bg-white/5 p-8 backdrop-blur-xl shadow-2xl shadow-blue-500/5">
          {/* Header */}
          <div className="text-center mb-8">
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ type: 'spring', stiffness: 200, delay: 0.2 }}
              className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-600 to-cyan-500 shadow-lg shadow-blue-500/30"
            >
              <KeyRound className="h-8 w-8 text-white" />
            </motion.div>
            <h1 className="text-2xl font-bold tracking-tight text-white">Security PIN</h1>
            <p className="mt-2 text-sm text-slate-400">
              Masukkan 6-digit PIN untuk mengakses sistem
            </p>
          </div>

          {/* Error */}
          {error && (
            <motion.div
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              className="mb-4 rounded-lg border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400 text-center"
            >
              {error}
            </motion.div>
          )}

          {/* PIN Input */}
          <div className="space-y-6">
            <div className="flex gap-3 justify-center" onPaste={handlePaste}>
              {pin.map((digit, index) => (
                <Input
                  key={index}
                  ref={(el) => { inputRefs.current[index] = el; }}
                  type="text"
                  inputMode="numeric"
                  maxLength={1}
                  value={digit}
                  onChange={(e) => handleChange(index, e.target.value)}
                  onKeyDown={(e) => handleKeyDown(index, e)}
                  className="w-12 h-14 text-center text-xl font-bold bg-white/5 border-white/10 text-white focus:border-blue-500/50 focus:ring-blue-500/20"
                  disabled={isVerifying}
                />
              ))}
            </div>

            <Button
              onClick={handleSubmit}
              disabled={isVerifying || pin.some(d => !d)}
              className="w-full bg-gradient-to-r from-blue-600 to-cyan-500 hover:from-blue-500 hover:to-cyan-400 text-white font-medium shadow-lg shadow-blue-500/25 transition-all duration-300 hover:shadow-blue-500/40"
            >
              {isVerifying ? 'Memverifikasi...' : 'Verifikasi PIN'}
            </Button>
          </div>

          {/* Info */}
          <div className="mt-6 text-center">
            <div className="flex items-center justify-center gap-2 text-slate-500">
              <Shield className="h-3.5 w-3.5" />
              <p className="text-xs">Dilindungi oleh sistem keamanan enterprise</p>
            </div>
            <p className="mt-2 text-[10px] text-slate-600">
              Bank BJB &copy; {new Date().getFullYear()} - PANSIN ACCESS v1.0
            </p>
          </div>
        </div>
      </motion.div>
    </div>
  );
}

