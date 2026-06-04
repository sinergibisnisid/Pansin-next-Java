'use client';

import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Shield, BarChart3, Bell, Lock, ArrowRight, CheckCircle2 } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { motion } from 'framer-motion';

export default function LandingPage() {
  const router = useRouter();

  const features = [
    { icon: Shield, title: 'Real-time Monitoring', desc: 'Monitor vault status 24/7' },
    { icon: Lock, title: 'Secure Access', desc: 'Multi-factor authentication' },
    { icon: Bell, title: 'Instant Alerts', desc: 'Get notified immediately' },
    { icon: BarChart3, title: 'Analytics', desc: 'Comprehensive reports' },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-cyan-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800">
      {/* Hero */}
      <section className="container mx-auto px-4 py-20">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="text-center space-y-6 max-w-3xl mx-auto"
        >
          <div className="inline-flex items-center gap-2 bg-blue-100 dark:bg-blue-900/30 px-4 py-2 rounded-full text-sm font-medium text-blue-600 dark:text-blue-400">
            <CheckCircle2 className="w-4 h-4" />
            Trusted by Financial Institutions
          </div>
          
          <h1 className="text-5xl md:text-6xl font-bold bg-gradient-to-r from-blue-600 to-cyan-500 bg-clip-text text-transparent">
            PANSIN ACCESS
          </h1>
          
          <p className="text-xl text-muted-foreground">
            Smart Vault Monitoring System for Modern Security
          </p>
          
          <div className="flex gap-4 justify-center">
            <Button size="lg" onClick={() => router.push('/login')} className="gap-2">
              Get Started <ArrowRight className="w-4 h-4" />
            </Button>
            <Button size="lg" variant="outline" onClick={() => router.push('/dashboard')}>
              View Dashboard
            </Button>
          </div>
        </motion.div>
      </section>

      {/* Features */}
      <section className="container mx-auto px-4 py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {features.map((feature, i) => (
            <motion.div
              key={feature.title}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.1 }}
            >
              <Card className="p-6 space-y-3 hover:shadow-lg transition-shadow">
                <div className="w-12 h-12 bg-gradient-to-br from-blue-600 to-cyan-500 rounded-xl flex items-center justify-center">
                  <feature.icon className="w-6 h-6 text-white" />
                </div>
                <h3 className="font-semibold">{feature.title}</h3>
                <p className="text-sm text-muted-foreground">{feature.desc}</p>
              </Card>
            </motion.div>
          ))}
        </div>
      </section>

      {/* Stats Preview */}
      <section className="container mx-auto px-4 py-12">
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.4 }}
        >
          <Card className="p-8">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
              <div className="text-center space-y-2">
                <p className="text-3xl font-bold text-blue-600">500+</p>
                <p className="text-sm text-muted-foreground">Vaults Monitored</p>
              </div>
              <div className="text-center space-y-2">
                <p className="text-3xl font-bold text-green-600">99.9%</p>
                <p className="text-sm text-muted-foreground">Uptime</p>
              </div>
              <div className="text-center space-y-2">
                <p className="text-3xl font-bold text-purple-600">24/7</p>
                <p className="text-sm text-muted-foreground">Support</p>
              </div>
              <div className="text-center space-y-2">
                <p className="text-3xl font-bold text-orange-600">1M+</p>
                <p className="text-sm text-muted-foreground">Events Tracked</p>
              </div>
            </div>
          </Card>
        </motion.div>
      </section>
    </div>
  );
}
