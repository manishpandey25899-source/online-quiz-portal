/**
 * ============================================================================
 * AI QUIZ PORTAL — TECH EFFECTS & ANIMATION ENGINE (LogSense / 21st.dev inspired)
 * Lightweight, GPU-accelerated Canvas & DOM effects
 * Respects prefers-reduced-motion and zero interference with application logic
 * ============================================================================
 */

(function () {
    'use strict';

    // Check accessibility preference
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // ------------------------------------------------------------------------
    // 1. AMBIENT TECH BACKGROUND (Mesh Grid + Subtle Particle Lines)
    // ------------------------------------------------------------------------
    function initTechBackground() {
        if (prefersReducedMotion) return;

        // Find or create canvas
        let canvas = document.getElementById('techBgCanvas');
        if (!canvas) {
            canvas = document.createElement('canvas');
            canvas.id = 'techBgCanvas';
            canvas.style.position = 'fixed';
            canvas.style.top = '0';
            canvas.style.left = '0';
            canvas.style.width = '100vw';
            canvas.style.height = '100vh';
            canvas.style.pointerEvents = 'none';
            canvas.style.zIndex = '0';
            canvas.style.opacity = '0.7';
            document.body.prepend(canvas);
        }

        const ctx = canvas.getContext('2d');
        if (!ctx) return;

        let width, height;
        let particles = [];
        const particleCount = 38;
        const maxDistance = 140;

        function resize() {
            width = canvas.width = window.innerWidth;
            height = canvas.height = window.innerHeight;
        }

        window.addEventListener('resize', resize, { passive: true });
        resize();

        class Particle {
            constructor() {
                this.x = Math.random() * width;
                this.y = Math.random() * height;
                this.vx = (Math.random() - 0.5) * 0.45;
                this.vy = (Math.random() - 0.5) * 0.45;
                this.radius = Math.random() * 1.8 + 1.2;
                this.color = Math.random() > 0.6 ? '#65D6B0' : (Math.random() > 0.4 ? '#3978F6' : '#75C9F5');
                this.alpha = Math.random() * 0.4 + 0.2;
            }

            update() {
                this.x += this.vx;
                this.y += this.vy;

                if (this.x < 0) this.x = width;
                else if (this.x > width) this.x = 0;

                if (this.y < 0) this.y = height;
                else if (this.y > height) this.y = 0;
            }

            draw() {
                ctx.beginPath();
                ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
                ctx.fillStyle = this.color;
                ctx.globalAlpha = this.alpha;
                ctx.fill();
            }
        }

        for (let i = 0; i < particleCount; i++) {
            particles.push(new Particle());
        }

        let animId;
        let isVisible = true;

        document.addEventListener('visibilitychange', () => {
            isVisible = !document.hidden;
            if (isVisible) loop();
        });

        function loop() {
            if (!isVisible) return;
            ctx.clearRect(0, 0, width, height);

            // Connect nearby particles
            for (let i = 0; i < particles.length; i++) {
                particles[i].update();
                particles[i].draw();

                for (let j = i + 1; j < particles.length; j++) {
                    const dx = particles[i].x - particles[j].x;
                    const dy = particles[i].y - particles[j].y;
                    const dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist < maxDistance) {
                        ctx.beginPath();
                        ctx.moveTo(particles[i].x, particles[i].y);
                        ctx.lineTo(particles[j].x, particles[j].y);
                        ctx.strokeStyle = '#3978F6';
                        ctx.globalAlpha = (1 - dist / maxDistance) * 0.16;
                        ctx.lineWidth = 1;
                        ctx.stroke();
                    }
                }
            }
            ctx.globalAlpha = 1;
            animId = requestAnimationFrame(loop);
        }

        loop();
    }

    // ------------------------------------------------------------------------
    // 2. 3D CARD TILT EFFECT (Subtle & Smooth)
    // ------------------------------------------------------------------------
    function initCardTilt() {
        if (prefersReducedMotion || window.innerWidth < 768) return;

        const cards = document.querySelectorAll('.tilt-card, .bento-card, .category-card, .login-box, .register-box, .quiz-box, .result-box, .history-box, .admin-kpi-card');
        cards.forEach(card => {
            card.addEventListener('mousemove', e => {
                const rect = card.getBoundingClientRect();
                const x = e.clientX - rect.left;
                const y = e.clientY - rect.top;
                const centerX = rect.width / 2;
                const centerY = rect.height / 2;
                const rotateX = ((y - centerY) / centerY) * -3.5;
                const rotateY = ((x - centerX) / centerX) * 3.5;

                card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateY(-2px)`;
            });

            card.addEventListener('mouseleave', () => {
                card.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) translateY(0)';
            });
        });
    }

    // ------------------------------------------------------------------------
    // 3. STATS COUNT-UP ANIMATION
    // ------------------------------------------------------------------------
    function initCountUp() {
        const counters = document.querySelectorAll('[data-counter]');
        counters.forEach(counter => {
            const target = parseInt(counter.getAttribute('data-counter') || counter.innerText, 10);
            if (isNaN(target)) return;

            let current = 0;
            const duration = 1200;
            const stepTime = 20;
            const steps = duration / stepTime;
            const increment = target / steps;

            const timer = setInterval(() => {
                current += increment;
                if (current >= target) {
                    current = target;
                    clearInterval(timer);
                }
                counter.innerText = Math.floor(current).toLocaleString();
            }, stepTime);
        });
    }

    // ------------------------------------------------------------------------
    // 4. ROTATING 3D DIGITAL TECH GLOBE (for Global Standing Widget)
    // ------------------------------------------------------------------------
    function initTechGlobe(canvasId) {
        const canvas = document.getElementById(canvasId || 'globeCanvas');
        if (!canvas) return;

        const ctx = canvas.getContext('2d');
        if (!ctx) return;

        let width = 220;
        let height = 200;
        if (canvas.parentElement && canvas.parentElement.clientWidth > 40) {
            const pWidth = canvas.parentElement.clientWidth;
            width = Math.min(240, Math.max(180, pWidth - 30));
            height = Math.round(width * 0.9);
        }
        canvas.width = width;
        canvas.height = height;

        const radius = Math.min(width, height) * 0.40;
        const centerX = width / 2;
        const centerY = height / 2;

        // Generate dots on a 3D sphere
        const dots = [];
        const numDots = 130;
        for (let i = 0; i < numDots; i++) {
            const phi = Math.acos(-1 + (2 * i) / numDots);
            const theta = Math.sqrt(numDots * Math.PI) * phi;
            dots.push({
                x: radius * Math.cos(theta) * Math.sin(phi),
                y: radius * Math.sin(theta) * Math.sin(phi),
                z: radius * Math.cos(phi)
            });
        }

        let angle = 0;
        let isVisible = true;

        document.addEventListener('visibilitychange', () => {
            isVisible = !document.hidden;
            if (isVisible) renderGlobe();
        });

        function renderGlobe() {
            if (!isVisible) return;
            ctx.clearRect(0, 0, width, height);

            angle += 0.012;
            const sinA = Math.sin(angle);
            const cosA = Math.cos(angle);

            // Draw glowing halo
            const grad = ctx.createRadialGradient(centerX, centerY, radius * 0.7, centerX, centerY, radius * 1.25);
            grad.addColorStop(0, 'rgba(57, 120, 246, 0.14)');
            grad.addColorStop(0.6, 'rgba(107, 99, 232, 0.08)');
            grad.addColorStop(1, 'rgba(247, 248, 245, 0)');
            ctx.fillStyle = grad;
            ctx.beginPath();
            ctx.arc(centerX, centerY, radius * 1.25, 0, Math.PI * 2);
            ctx.fill();

            // Draw globe sphere outline
            ctx.beginPath();
            ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
            ctx.strokeStyle = 'rgba(57, 120, 246, 0.22)';
            ctx.lineWidth = 1.2;
            ctx.stroke();

            // Latitude rings
            [-0.5, 0, 0.5].forEach(latOffset => {
                const r = radius * Math.cos(latOffset);
                const y = centerY + radius * Math.sin(latOffset);
                ctx.beginPath();
                ctx.ellipse(centerX, y, r, r * 0.25, 0, 0, Math.PI * 2);
                ctx.strokeStyle = 'rgba(117, 201, 245, 0.16)';
                ctx.lineWidth = 0.8;
                ctx.stroke();
            });

            // Project and draw dots
            dots.forEach((dot, index) => {
                // Rotate around Y axis
                const rotX = dot.x * cosA - dot.z * sinA;
                const rotZ = dot.z * cosA + dot.x * sinA;

                // Simple perspective projection
                const scale = 260 / (260 + rotZ);
                const projX = centerX + rotX * scale;
                const projY = centerY + dot.y * scale;

                const alpha = Math.max(0.12, (rotZ + radius) / (2 * radius));
                ctx.beginPath();
                ctx.arc(projX, projY, Math.max(1, 2.2 * scale), 0, Math.PI * 2);
                
                // Highlight select nodes like connection points
                if (index % 14 === 0) {
                    ctx.fillStyle = '#65D6B0'; // Mint tech accent
                    ctx.shadowColor = '#65D6B0';
                    ctx.shadowBlur = 7;
                } else if (index % 7 === 0) {
                    ctx.fillStyle = '#75C9F5';
                    ctx.shadowColor = '#75C9F5';
                    ctx.shadowBlur = 4;
                } else {
                    ctx.fillStyle = '#3978F6';
                    ctx.shadowBlur = 0;
                }
                ctx.globalAlpha = alpha;
                ctx.fill();
            });

            ctx.shadowBlur = 0;
            ctx.globalAlpha = 1;

            if (!prefersReducedMotion) {
                requestAnimationFrame(renderGlobe);
            }
        }

        renderGlobe();
    }

    // Expose helpers globally
    window.OQEffects = {
        initTechBackground,
        initCardTilt,
        initCountUp,
        initTechGlobe
    };

    // Auto-init on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            initTechBackground();
            initCardTilt();
            initCountUp();
        });
    } else {
        initTechBackground();
        initCardTilt();
        initCountUp();
    }
})();
