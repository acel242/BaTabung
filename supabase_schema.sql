-- =============================================
-- BaTabung Supabase Schema v2
-- Jalankan script ini di Supabase SQL Editor
-- 
-- PERUBAHAN v2: 
-- - Hapus tabel tabungan (1 Bank = 1 Tabungan)
-- - Transaksi langsung terhubung ke banks
-- =============================================

-- Enable UUID extension
create extension if not exists "uuid-ossp";

-- =============================================
-- BANKS TABLE
-- Setiap bank/e-wallet adalah satu tabungan
-- =============================================
create table if not exists public.banks (
    id uuid primary key default uuid_generate_v4(),
    user_id uuid not null references auth.users(id) on delete cascade,
    nama text not null,
    alias text default '',
    jenis text not null check (jenis in ('BANK', 'EWALLET')),
    is_aktif boolean default true,
    package_name text,
    created_at bigint default (extract(epoch from now()) * 1000)::bigint,
    updated_at bigint default (extract(epoch from now()) * 1000)::bigint
);

-- RLS for banks
alter table public.banks enable row level security;

create policy "Users can view own banks"
    on public.banks for select
    using (auth.uid() = user_id);

create policy "Users can insert own banks"
    on public.banks for insert
    with check (auth.uid() = user_id);

create policy "Users can update own banks"
    on public.banks for update
    using (auth.uid() = user_id);

create policy "Users can delete own banks"
    on public.banks for delete
    using (auth.uid() = user_id);

-- Index
create index if not exists idx_banks_user_id on public.banks(user_id);

-- =============================================
-- TRANSAKSI TABLE
-- Transaksi langsung terhubung ke bank (tidak via tabungan)
-- =============================================
create table if not exists public.transaksi (
    id uuid primary key default uuid_generate_v4(),
    user_id uuid not null references auth.users(id) on delete cascade,
    bank_id uuid not null references public.banks(id) on delete cascade,
    tanggal bigint not null,
    jenis text not null check (jenis in ('MASUK', 'KELUAR')),
    jumlah bigint not null,
    kategori text default '',
    catatan text,
    created_at bigint default (extract(epoch from now()) * 1000)::bigint,
    updated_at bigint default (extract(epoch from now()) * 1000)::bigint
);

-- RLS for transaksi
alter table public.transaksi enable row level security;

create policy "Users can view own transaksi"
    on public.transaksi for select
    using (auth.uid() = user_id);

create policy "Users can insert own transaksi"
    on public.transaksi for insert
    with check (auth.uid() = user_id);

create policy "Users can update own transaksi"
    on public.transaksi for update
    using (auth.uid() = user_id);

create policy "Users can delete own transaksi"
    on public.transaksi for delete
    using (auth.uid() = user_id);

-- Indexes
create index if not exists idx_transaksi_user_id on public.transaksi(user_id);
create index if not exists idx_transaksi_bank_id on public.transaksi(bank_id);
create index if not exists idx_transaksi_tanggal on public.transaksi(tanggal);

-- =============================================
-- USER PROFILES TABLE
-- Menyimpan informasi profil user
-- =============================================
create table if not exists public.user_profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    email text,
    display_name text,
    created_at bigint default (extract(epoch from now()) * 1000)::bigint,
    updated_at bigint default (extract(epoch from now()) * 1000)::bigint
);

-- RLS for user_profiles
alter table public.user_profiles enable row level security;

create policy "Users can view own profile"
    on public.user_profiles for select
    using (auth.uid() = id);

create policy "Users can insert own profile"
    on public.user_profiles for insert
    with check (auth.uid() = id);

create policy "Users can update own profile"
    on public.user_profiles for update
    using (auth.uid() = id);

-- =============================================
-- DONE! 
-- Schema v3: Full Account Sync
-- - user_profiles: profil pengguna
-- - chat_history: LOKAL SAJA (tidak disinkronkan)
-- =============================================
