package com.toocol.ssh.core.mosh.core;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static com.toocol.ssh.core.mosh.core.AeOcb.Block.*;
import static com.toocol.ssh.core.mosh.core.ByteOrder.bswap64;

/**
 * ae.h/ocb.cc
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/2 22:28
 * @version: 0.0.1
 */
public class AeOcb {
    public static final int AE_SUCCESS = 0;
    public static final int AE_INVALID = -1;
    public static final int AE_NOT_SUPPORTED = -2;

    private static final int L_TABLE_SIZE = 16;
    private static final int OCB_KEY_LEN = 16;
    private static final int BPI = 4;
    private static final int OCB_TAG_LEN = 16;
    private static final int[] TZ_TABLE = new int[]{
            0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8,
            31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9
    };

    public static class Block {
        long l;
        long r;

        Block(long l, long r) {
            this.l = l;
            this.r = r;
        }

        byte[] getBytes() {
            byte[] bytes = new byte[16];
            byte[] lBytes = Longs.toByteArray(l);
            byte[] rBytes = Longs.toByteArray(r);
            System.arraycopy(lBytes, 0, bytes, 0, 8);
            System.arraycopy(rBytes, 0, bytes, 8, 8);
            return bytes;
        }

        void doubleBlock() {
            Block b = this;
            long t = b.l >> 63;
            b.l = (b.l + b.l) ^ (b.r >> 63);
            b.r = (b.r + b.r) ^ (t & 135);
        }

        static Block xorBlock(Block x, Block y) {
            Block b = zeroBlock();
            b.l = x.l ^ y.l;
            b.r = x.r ^ y.r;
            return b;
        }

        static Block zeroBlock() {
            return new Block(0, 0);
        }

        static Block fromBytes(byte[] bytes) {
            Block block = zeroBlock();
            byte[] bytes8 = new byte[8];
            System.arraycopy(bytes, 0, bytes8, 0, 8);
            block.l = Longs.fromByteArray(bytes8);
            System.arraycopy(bytes, 8, bytes8, 0, 8);
            block.r = Longs.fromByteArray(bytes8);
            return block;
        }

        /**
         * if native byte order is little-endian, swap the bytes array;
         */
        static Block swapIfLe(Block b) {
            if (ByteOrder.littleEndian()) {
                return new Block(
                        Longs.fromByteArray(bswap64(b.l)),
                        Longs.fromByteArray(bswap64(b.r))
                );
            } else {
                return b;
            }
        }

        static Block genOffset(long[] ktopStr, int bot) {
            Block rval = zeroBlock();
            if (bot != 0) {
                rval.l = (ktopStr[0] << bot) | (ktopStr[1] >> (64 - bot));
                rval.r = (ktopStr[1] << bot) | (ktopStr[2] >> (64 - bot));
            } else {
                rval.l = ktopStr[0];
                rval.r = ktopStr[1];
            }
            return swapIfLe(rval);
        }

        static boolean unequalBlocks(Block x, Block y) {
            return (((x).l ^ (y).l) | ((x).r ^ (y).r)) != 0;
        }
    }

    public static class AeCtx {
        static final String ALGORITHM = "AES";
        static final String AES_TYPE = "AES/ECB/NoPadding";

        final Block[] l = new Block[L_TABLE_SIZE];
        final long[] ktopStr = new long[3];

        Block offset;
        Block checksum;
        Block lstar;
        Block ldollor;
        Block adCheckSum;
        Block adOffset;
        Block cachedTop;
        int adBlocksProcessed;
        int blocksProcessed;
        Cipher encryptCipher;
        Cipher decryptCipher;

        void setCipher(byte[] key) throws Exception {
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);

            encryptCipher = Cipher.getInstance(AES_TYPE);
            encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec);

            decryptCipher = Cipher.getInstance(AES_TYPE);
            decryptCipher.init(Cipher.DECRYPT_MODE, keySpec);
        }

        byte[] encrypt(byte[] origin) throws Exception {
            return encryptCipher.doFinal(origin);
        }

        byte[] decrypt(byte[] origin) throws Exception {
            return decryptCipher.doFinal(origin);
        }

        void encryptBlock(Block[] block, int bulks) throws Exception {
            for(int i = 0; i < bulks; i++) {
                block[i] = fromBytes(encrypt(block[i].getBytes()));
            }
        }

        void decryptBlock(Block[] block, int bulks) throws Exception {
            for(int i = 0; i < bulks; i++) {
                block[i] = fromBytes(decrypt(block[i].getBytes()));
            }
        }

        public AeCtx() {
        }
    }

    public static int aeInit(AeCtx ctx, byte[] key, int keyLen, int nonceLen, int tagLen) {
        if (keyLen != OCB_KEY_LEN) {
            return AE_NOT_SUPPORTED;
        }
        if (nonceLen != 12) {
            return AE_NOT_SUPPORTED;
        }
        try {
            int i;
            Block tmpBlk;

            ctx.setCipher(key);

            ctx.cachedTop = zeroBlock();
            ctx.checksum = zeroBlock();
            ctx.adBlocksProcessed = 0;

            ctx.lstar = fromBytes(ctx.encrypt(ctx.cachedTop.getBytes()));

            tmpBlk = swapIfLe(ctx.lstar);
            tmpBlk.doubleBlock();
            ctx.ldollor = swapIfLe(tmpBlk);
            tmpBlk.doubleBlock();
            ctx.l[0] = swapIfLe(tmpBlk);
            for (i = 1; i < L_TABLE_SIZE; i++) {
                tmpBlk.doubleBlock();
                ctx.l[i] = swapIfLe(tmpBlk);
            }
        } catch (Exception e) {
            return AE_INVALID;
        }
        return AE_SUCCESS;
    }

    @SuppressWarnings("all")
    public static int aeEncrypt(
            AeCtx ctx,
            byte[] nonce,
            byte[] pt,
            int ptLen,
            byte[] ad,
            int adLen,
            byte[] ct,
            byte[] tag,
            int finalize
    ) {
        try {
            Block[] ptp, ctp;
            Block offset, checksum;
            Block tmpBl = zeroBlock();
            byte[] tmpU8;

            int i, j, k;
            if (nonce != null && nonce.length > 0) {
                ctx.offset = genOffsetFromNonce(ctx, nonce);
                ctx.adOffset = zeroBlock();
                ctx.checksum = zeroBlock();
                ctx.adBlocksProcessed = 0;
                ctx.blocksProcessed = 0;
                if (adLen >= 0) {
                    ctx.adCheckSum = zeroBlock();
                }
            }

            if (adLen > 0) {
                processAd(ctx, ad, adLen, finalize);
            }

            offset = ctx.offset;
            checksum = ctx.checksum;
            i = ptLen / (BPI * 16);

            ptp = transferBlockArrays(pt, i);
            ctp = new Block[BPI];
            initBlocks(ctp);

            j = 0;
            if (i > 0) {
                Block[] oa = new Block[BPI];
                int blockNum = ctx.blocksProcessed;
                oa[BPI - 1] = offset;
                do {
                    Block[] ta = new Block[BPI];
                    blockNum += BPI;

                    oa[0] = xorBlock(oa[BPI - 1], ctx.l[0]);
                    ta[0] = xorBlock(oa[0], ptp[j]);
                    checksum = xorBlock(checksum, ptp[j]);

                    oa[1] = xorBlock(oa[0], ctx.l[1]);
                    ta[1] = xorBlock(oa[1], ptp[j + 1]);
                    checksum = xorBlock(checksum, ptp[j + 1]);

                    oa[2] = xorBlock(oa[1], ctx.l[0]);
                    ta[2] = xorBlock(oa[2], ptp[j + 2]);
                    checksum = xorBlock(checksum, ptp[j + 2]);

                    oa[3] = xorBlock(oa[2], ctx.l[ntz(blockNum)]);
                    ta[3] = xorBlock(oa[3], ptp[j + 3]);
                    checksum = xorBlock(checksum, ptp[j + 3]);

                    ctx.encryptBlock(ta, BPI);

                    ctp[0] = xorBlock(ta[0], oa[0]);
                    ctp[1] = xorBlock(ta[1], oa[1]);
                    ctp[2] = xorBlock(ta[2], oa[2]);
                    ctp[3] = xorBlock(ta[3], oa[3]);
                    fillDataFromBlockArrays(ct, ctp, j);

                } while (++j < i);

                offset = oa[BPI - 1];
                ctx.offset = offset;
                ctx.blocksProcessed = blockNum;
                ctx.checksum = checksum;
            }

            ctp = new Block[BPI];
            initBlocks(ctp);
            if (finalize > 0) {
                Block[] ta = new Block[BPI + 1], oa = new Block[BPI];
                initBlocks(ta);
                initBlocks(oa);
                int remaining = ptLen % (BPI * 16);
                k = 0;

                if (remaining > 0) {
                    if (remaining >= 32) {
                        oa[k] = xorBlock(offset, ctx.l[0]);
                        ta[k] = xorBlock(oa[k], ctp[k]);
                        oa[k + 1] = xorBlock(oa[k], ctx.l[1]);
                        offset = oa[k + 1];
                        ta[k + 1] = xorBlock(offset, ctp[k + 1]);
                        remaining -= 32;
                        k += 2;
                    }
                    if (remaining >= 16) {
                        oa[k] = xorBlock(offset, ctx.l[0]);
                        offset = oa[k];
                        ta[k] = xorBlock(offset, ctp[k]);
                        remaining -= 16;
                        ++k;
                    }
                    if (remaining > 0) {
                        tmpU8 = tmpBl.getBytes();
                        System.arraycopy(
                                getBytesFromBlockArrays(ptp, 0, ptp.length), k *16,
                                tmpU8, 0,
                                remaining
                        );
                        tmpU8[remaining] = (byte) 0x80;
                        tmpBl = fromBytes(tmpU8);
                        checksum = xorBlock(checksum, tmpBl);
                        offset = xorBlock(offset, ctx.lstar);
                        ta[k] = offset;
                        ++k;
                    }
                }

                offset = xorBlock(offset, ctx.ldollor);
                ta[k] = xorBlock(offset, checksum);
                ctx.encryptBlock(ta, k+1);
                offset = xorBlock(ta[k], ctx.adCheckSum);
                if (remaining > 0) {
                    --k;
                    tmpBl = xorBlock(tmpBl, ta[k]);
                    tmpU8 = tmpBl.getBytes();
                    byte[] bytes = getBytesFromBlockArrays(ctp, 0, ctp.length);
                    System.arraycopy(tmpU8, 0, bytes, 16 * k, remaining);
                    ctp = transferBlockArrays(bytes, 1);
                }

                switch (k) {
                    case 3:
                        ctp[2] = xorBlock(ta[2], oa[2]);
                    case 2:
                        ctp[1] = xorBlock(ta[1], oa[1]);
                    case 1:
                        ctp[0] = xorBlock(ta[0], oa[0]);
                    default:
                        break;
                }
                fillDataFromBlockArrays(ct, ctp, j);

                if (tag != null) {
                    System.arraycopy(offset.getBytes(), 0, tag, 0, OCB_TAG_LEN);
                } else {
                    System.arraycopy(offset.getBytes(), 0, ct, ptLen, OCB_TAG_LEN);
                    ptLen += OCB_TAG_LEN;
                }
            }
        } catch (Exception e) {
            return -1;
        }
        return ptLen;
    }

    static int ntz(int x) {
        return TZ_TABLE[((x & -x) & 0x077CB531) >> 27];
    }

    static void initBlocks(Block[] blocks) {
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = zeroBlock();
        }
    }

    static void fillDataFromBlockArrays(byte[] target, Block[] blocks, int round) {
        for (int idx = 0; idx < blocks.length; idx++) {
            int destPos = (round * BPI * 16) + (idx * 16);
            System.arraycopy(blocks[idx].getBytes(), 0, target, destPos, 16);
        }
    }

    static Block[] transferBlockArrays(byte[] bytes, int round) {
        round = round == 0 ? 1 : round;
        Block[] blks = new Block[BPI * round];
        int gap = 16;
        for (int i = 0; i < BPI * round; i++) {
            byte[] bytes16 = new byte[gap];
            for (int j = 0; j < gap; j++) {
                byte val = i * gap + j >= bytes.length ? 0 : bytes[i * gap + j];
                bytes16[j] = val;
            }
            blks[i] = fromBytes(bytes16);
        }
        return blks;
    }

    static byte[] getBytesFromBlockArrays(Block[] blocks, int start, int end) {
        byte[] bytes = new byte[16 * blocks.length];
        for (int i = start; i < end; i++) {
            System.arraycopy(blocks[i].getBytes(), 0, bytes, i * 16, 16);
        }
        return bytes;
    }

    static Block genOffsetFromNonce(AeCtx ctx, byte[] nonce) throws Exception {
        int idx;
        byte[] bytes16 = new byte[16];
        int[] tmp = new int[4];
        tmp[0] = ByteOrder.littleEndian() ? 0x01000000 : 0x00000001;
        byte[] bytes4 = new byte[4];
        for (int i = 1; i <= 3; i++) {
            System.arraycopy(nonce, (i - 1) * 4, bytes4, 0, 4);
            tmp[i] = Ints.fromByteArray(bytes4);
        }

        for (int i = 0; i < tmp.length; i++) {
            byte[] bytes = Ints.toByteArray(tmp[i]);
            System.arraycopy(bytes, 0, bytes16, i * 4, 4);
        }

        idx = bytes16[15] & 0x3f;
        bytes16[15] = (byte) (bytes16[15] & 0xc0);

        Block tmpBlk = fromBytes(bytes16);

        if (Block.unequalBlocks(tmpBlk, ctx.cachedTop)) {
            ctx.cachedTop = tmpBlk;
            byte[] encrypt = ctx.encrypt(tmpBlk.getBytes());
            assert encrypt.length == 16;
            Block ktopBlk = fromBytes(encrypt);
            if (ByteOrder.littleEndian()) {
                ktopBlk = swapIfLe(ktopBlk);
            }
            ctx.ktopStr[0] = ktopBlk.l;
            ctx.ktopStr[1] = ktopBlk.r;
            ctx.ktopStr[2] = ctx.ktopStr[0] ^ (ctx.ktopStr[0] << 8) ^ (ctx.ktopStr[1] >> 56);
        }

        return Block.genOffset(ctx.ktopStr, idx);
    }

    static void processAd(AeCtx ctx, byte[] ad, int adLen, int finalise) {

    }
}