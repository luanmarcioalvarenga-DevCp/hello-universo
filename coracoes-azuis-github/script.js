"use strict";

const canvas = document.querySelector("#particles");
const ctx = canvas.getContext("2d");
const BLUE_SHADES = ["#3b82f6", "#60a5fa", "#93c5fd", "#2563eb", "#bfdbfe", "#1d4ed8"];
const FLOAT_COUNT = 140;
const floatParticles = [];
const burstParticles = [];
const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
let width = 0;
let height = 0;
let paused = reducedMotion.matches;
let previous = 0;
let animationId = null;

class Particle {
    constructor(x, y, type) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.color = BLUE_SHADES[Math.floor(Math.random() * BLUE_SHADES.length)];
        this.size = type === "burst" ? 4 + Math.random() * 10 : 3 + Math.random() * 12;
        this.maxLife = type === "burst" ? 1.6 + Math.random() * 1.8 : 8 + Math.random() * 12;
        this.life = 0;
        this.angle = (Math.random() - 0.5) * 0.7;
        this.phase = Math.random() * Math.PI * 2;
        const direction = Math.random() * Math.PI * 2;
        const speed = 50 + Math.random() * 240;
        this.vx = type === "burst" ? Math.cos(direction) * speed : (Math.random() - 0.5) * 12;
        this.vy = type === "burst" ? Math.sin(direction) * speed - 50 : -10 - Math.random() * 18;
    }

    update(dt) {
        this.life += dt;
        this.x += (this.vx + (this.type === "float" ? Math.sin(this.life + this.phase) * 9 : 0)) * dt;
        this.y += this.vy * dt;
        if (this.type === "burst") {
            this.vy += 65 * dt;
            this.vx *= Math.exp(-0.45 * dt);
            this.angle += dt * 0.5;
        } else if (this.life >= this.maxLife || this.y < -30 || this.x < -40 || this.x > width + 40) {
            Object.assign(this, new Particle(Math.random() * width, height + 20, "float"));
        }
    }

    draw() {
        const progress = this.life / this.maxLife;
        const fade = Math.max(0, Math.min(1, progress * 5, (1 - progress) * 4));
        ctx.save();
        ctx.translate(this.x, this.y);
        ctx.rotate(this.angle);
        ctx.scale(this.size, this.size);
        ctx.globalAlpha = fade * (this.type === "float" ? 0.38 : 0.9);
        ctx.fillStyle = this.color;
        ctx.shadowColor = this.color;
        ctx.shadowBlur = 9;
        ctx.beginPath();
        ctx.moveTo(0, 0.85);
        ctx.bezierCurveTo(-1.6, -0.1, -0.95, -1.2, 0, -0.48);
        ctx.bezierCurveTo(0.95, -1.2, 1.6, -0.1, 0, 0.85);
        ctx.fill();
        ctx.restore();
    }
}

function resize() {
    width = window.innerWidth;
    height = window.innerHeight;
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    canvas.width = Math.round(width * dpr);
    canvas.height = Math.round(height * dpr);
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    floatParticles.length = 0;
    for (let i = 0; i < FLOAT_COUNT; i++) {
        const p = new Particle(Math.random() * width, Math.random() * height, "float");
        p.life = Math.random() * p.maxLife;
        floatParticles.push(p);
    }
    render();
}

function spawnHeartBurst(x, y) {
    // Limit the number of particles even when the user clicks repeatedly.
    const count = 140;
    const overflow = Math.max(0, burstParticles.length + count - 1400);
    burstParticles.splice(0, overflow);
    for (let i = 0; i < count; i++) {
        const p = new Particle(x, y, "burst");
        // Make the burst visible immediately when movement is paused.
        if (paused) {
            p.update(0.25 + Math.random() * 0.3);
        }
        burstParticles.push(p);
    }
    render();
}

function render() {
    ctx.clearRect(0, 0, width, height);
    floatParticles.forEach(p => p.draw());
    burstParticles.forEach(p => p.draw());
}

function frame(timestamp) {
    animationId = null;
    if (paused || document.hidden) return;
    const dt = previous ? Math.min((timestamp - previous) / 1000, 0.05) : 0;
    previous = timestamp;
    floatParticles.forEach(p => p.update(dt));
    for (let i = burstParticles.length - 1; i >= 0; i--) {
        burstParticles[i].update(dt);
        if (burstParticles[i].life >= burstParticles[i].maxLife) burstParticles.splice(i, 1);
    }
    render();
    animationId = requestAnimationFrame(frame);
}

function syncAnimation() {
    if (animationId !== null) cancelAnimationFrame(animationId);
    animationId = null;
    previous = 0;
    if (!paused && !document.hidden) animationId = requestAnimationFrame(frame);
    const button = document.querySelector("#pause");
    button.textContent = paused ? "Continuar movimento" : "Pausar movimento";
    button.setAttribute("aria-pressed", String(paused));
}

document.addEventListener("pointerdown", event => {
    if (event.target.closest("button, a") || (event.pointerType === "mouse" && event.button !== 0)) return;
    spawnHeartBurst(event.clientX, event.clientY);
});
document.querySelector("#burst").addEventListener("click", event => {
    const bounds = event.currentTarget.getBoundingClientRect();
    spawnHeartBurst(bounds.left + bounds.width / 2, bounds.top + bounds.height / 2);
});
document.querySelector("#pause").addEventListener("click", () => { paused = !paused; syncAnimation(); });
document.querySelector("#clear").addEventListener("click", () => { burstParticles.length = 0; render(); });
document.addEventListener("visibilitychange", syncAnimation);
window.addEventListener("resize", resize);
reducedMotion.addEventListener("change", event => { paused = event.matches; syncAnimation(); });
resize();
syncAnimation();
