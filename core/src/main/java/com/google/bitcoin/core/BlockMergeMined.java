/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.bitcoin.core;

import com.google.common.primitives.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * <p>A block is a group of transactions, and is one of the fundamental data structures of the Bitcoin system.
 * It records a set of {@link com.google.devcoin.core.Transaction}s together with some data that links it into a place in the global block
 * chain, and proves that a difficult calculation was done over its contents. See
 * <a href="http://www.bitcoin.org/bitcoin.pdf">the Bitcoin technical paper</a> for
 * more detail on blocks. <p/>
 *
 * To get a block, you can either build one from the raw bytes you can get from another implementation, or request one
 * specifically using {@link com.google.devcoin.core.Peer#getBlock(com.google.devcoin.core.Sha256Hash)}, or grab one from a downloaded {@link com.google.devcoin.core.BlockChain}.
 */
public class BlockMergeMined {
    private static final Logger log = LoggerFactory.getLogger(BlockMergeMined.class);
    public static final long MERGED_MINE_START_TIME = 1443262762;
    public static final long BLOCK_VERSION_AUXPOW = (1 << 8);
    public static final long BLOCK_VERSION_CHAIN_START = (1 << 16);
    public static final long BLOCK_VERSION_CHAIN_END = (1 << 30);


    public static final int AUXPOW_CHAIN_ID = 0x005A;
    public static final int AUXPOW_START_MAINNET = 1402000;
    public static final int AUXPOW_START_TESTNET = 175;
    public static final long MERGED_MINE_CHAIN_ID = 0x005A;
    public static final byte pchMergedMiningHeader[] = { (byte)0xfa, (byte)0xbe, 'm', 'm' } ;
    // Fields defined as part of the protocol format.
    // modifiers

    private transient NetworkParameters params;
    public transient Block block;
    public transient BlockMergeMinedPayload payload;


    /** Constructs a block object from the Bitcoin wire format. */
    public BlockMergeMined(NetworkParameters netparams, byte[] payloadBytes, int cursor, Block block) throws ProtocolException {
        params = netparams;
        payload = new BlockMergeMinedPayload(this.params, payloadBytes, cursor, block);
        setBlock(block);
    }
    private BlockMergeMined()
        {

                    }
        public BlockMergeMined duplicate()
        {
                BlockMergeMined mmblock = new BlockMergeMined();

                        mmblock.params = params;
                mmblock.payload = payload;
                mmblock.setBlock(block);
                return mmblock;
        }

    private void setBlock(Block block)
    {
        this.block = block;
        if(this.payload != null && this.payload.block == null)
            this.payload.block = block;
    }
    public long GetChainID(long ver)
    {
        return ver / BLOCK_VERSION_CHAIN_START;
    }
    public long GetChainID()
    {
        return block.getVersion() / BLOCK_VERSION_CHAIN_START;
    }
    public boolean IsValid()
    {
        return payload != null && payload.IsValid();
    }
    public Sha256Hash getParentBlockHash()
    {
        //return payload.hashOfParentBlockHeader;
        return payload.parentBlockHeader.getPOWHash(block.getAlgo()); //For myriad, we must always check the block header hash, not the value included in the block header
    }
    /** Returns the version of the block data structure as defined by the Bitcoin protocol. */
    public long getVersion() {
        return block.getVersion();
    }

    /**
     * Returns the nonce, an arbitrary value that exists only to make the hash of the block header fall below the
     * difficulty target.
     */
    public long getNonce() {
        return block.getNonce();
    }
    /**
     * Returns a multi-line string containing a description of the contents of
     * the block. Use for debugging purposes only.
     */

    public String toString() {
        StringBuilder s = new StringBuilder("");
        s.append("      version: v");
        s.append(block.getVersion());
        s.append("\n");
        s.append("      time: [");
        s.append(block.getTime());
        s.append("] ");
        s.append(new Date(block.getTimeSeconds() * 1000));
        s.append("\n");
        s.append("      difficulty target (nBits): ");
        s.append(block.getDifficultyTarget());
        s.append("\n");
        s.append("      nonce: ");
        s.append(block.getNonce());
        s.append("\n");
        if(payload != null)
        {
            s.append("\n");
            s.append(payload.toString());
        }
        return s.toString();
    }

    public int getCursor()
    {
        if(payload != null)
            return payload.cursor;
        else
            return 0;
    }
    public int getMessageSize()
    {
        if(payload != null)
            return payload.length;
        else
            return 0;
    }
    /** Returns true if the hash of the block is OK (lower than difficulty target). */

    protected boolean checkProofOfWork(boolean throwException) throws VerificationException {
        int algo = block.getAlgo();
        if(algo != Block.ALGO_SHA256D && algo != Block.ALGO_SCRYPT)
        {
            throw new VerificationException("Merged-mine block does not have the correct algorithm required for Myriadcoin blocks (SHA256D or Scrypt), Current Algo: " + block.getAlgoName());
        }
        if(GetChainID() != MERGED_MINE_CHAIN_ID)
        {
            throw new VerificationException("Merged-mine block does not have the correct chain ID required for Myriadcoin blocks, Current ID: " + GetChainID() + " Expected: " + MERGED_MINE_CHAIN_ID);
        }
        if(GetChainID(payload.parentBlockHeader.getVersion()) == MERGED_MINE_CHAIN_ID)
        {
            throw new VerificationException("Merged-mine block Aux POW parent has our chain ID: " + MERGED_MINE_CHAIN_ID);
        }
        TransactionInput coinbaseInput = payload.parentBlockCoinBaseTx.getInput(0);
        byte[] scriptBytes = coinbaseInput.getScriptBytes();
        int headerIndex = Bytes.indexOf(scriptBytes, this.pchMergedMiningHeader);
        if(headerIndex > 0)
        {
            if((scriptBytes.length - headerIndex) >= headerIndex)
            {
                byte[] remainingBytes = java.util.Arrays.copyOfRange(scriptBytes, headerIndex, scriptBytes.length - headerIndex);
                headerIndex = Bytes.indexOf(remainingBytes, this.pchMergedMiningHeader);
                if(headerIndex > 0)
                {
                    throw new VerificationException("Multiple merged mining headers in coinbase");
                }
            }
            if(!coinbaseInput.isCoinBase())
            {
                throw new VerificationException("Parent coinbase transaction not an actual coinbase transaction!");
            }
        }
        return true;
    }


    public boolean equals(Object o) {
        if (!(o instanceof BlockMergeMined))
            return false;
        BlockMergeMined other = (BlockMergeMined) o;
        return block.getTimeSeconds() == other.block.getTimeSeconds();
    }

    /**
     * Returns the time at which the block was solved and broadcast, according to the clock of the solving node. This
     * is measured in seconds since the UNIX epoch (midnight Jan 1st 1970).
     */
    public long getTimeSeconds() {
        return block.getTimeSeconds();
    }

    /**
     * Returns the time at which the block was solved and broadcast, according to the clock of the solving node.
     */
    public Date getTime() {
        return new Date(getTimeSeconds()*1000);
    }



}
