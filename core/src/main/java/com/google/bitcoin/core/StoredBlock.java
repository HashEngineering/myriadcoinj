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

import com.google.bitcoin.store.BlockStore;
import com.google.bitcoin.store.BlockStoreException;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkState;

/**
 * Wraps a {@link Block} object with extra data that can be derived from the block chain but is slow or inconvenient to
 * calculate. By storing it alongside the block header we reduce the amount of work required significantly.
 * Recalculation is slow because the fields are cumulative - to find the chainWork you have to iterate over every
 * block in the chain back to the genesis block, which involves lots of seeking/loading etc. So we just keep a
 * running total: it's a disk space vs cpu/io tradeoff.<p>
 *
 * StoredBlocks are put inside a {@link BlockStore} which saves them to memory or disk.
 */
public class StoredBlock implements Serializable {
    private static final long serialVersionUID = -6097565241243701771L;

    // A BigInteger representing the total amount of work done so far on this chain. As of May 2011 it takes 8
    // bytes to represent this field, so 12 bytes should be plenty for now.
    public static final int CHAIN_WORK_BYTES = 12;
    public static final int CHAIN_WORK_BYTES2 = 16;
    public static final byte[] EMPTY_BYTES = new byte[CHAIN_WORK_BYTES];
    public static final byte[] EMPTY_BYTES2 = new byte[CHAIN_WORK_BYTES2];
    public static final int COMPACT_SERIALIZED_SIZE = Block.HEADER_SIZE + CHAIN_WORK_BYTES + 4;  // for height
    public static final int COMPACT_SERIALIZED_SIZE2 = Block.HEADER_SIZE + CHAIN_WORK_BYTES2 + 4;  // for height

    private Block header;
    private BigInteger chainWork;
    private int height;
    BigInteger nAlgoWork[] = new BigInteger[Block.NUM_ALGOS];

    public StoredBlock(Block header, BigInteger chainWork, int height) {
        this.header = header;
        this.chainWork = chainWork;
        this.height = height;
        for(int i = 0; i < Block.NUM_ALGOS; ++i)
            nAlgoWork[i] = BigInteger.ZERO;
    }

    /**
     * The block header this object wraps. The referenced block object must not have any transactions in it.
     */
    public Block getHeader() {
        return header;
    }

    /**
     * The total sum of work done in this block, and all the blocks below it in the chain. Work is a measure of how
     * many tries are needed to solve a block. If the target is set to cover 10% of the total hash value space,
     * then the work represented by a block is 10.
     */
    public BigInteger getChainWork() {
        return chainWork;
    }

    /**
     * Position in the chain for this block. The genesis block has a height of zero.
     */
    public int getHeight() {
        return height;
    }

    //class CBlockIndexWorkComparator
    //{
        static public boolean BlockIndexWorkComparator(StoredBlock pa, StoredBlock pb)
        {
            // First sort by most total work, ...
            if (pa.getChainWork().compareTo(pb.getChainWork()) > 0) return false;
            if (pa.getChainWork().compareTo(pb.getChainWork()) < 0) return true;

            // ... then by earliest time received, ...
            if (pa.getHeader().sequenceId < pb.getHeader().sequenceId) return false;
            if (pa.getHeader().sequenceId > pb.getHeader().sequenceId) return true;

            // Use pointer address as tie breaker (should only happen with blocks
            // loaded from disk, as those all have id 0).

            if (pa.hashCode() < pb.hashCode()) return false;
            if (pa.hashCode() > pb.hashCode()) return true;

            // Identical blocks.
            return false;
        }
    //};

    /** Returns true if this objects chainWork is higher than the others. */
    public boolean moreWorkThan(StoredBlock other) {
        if(height < CoinDefinition.nBlockAlgoNormalisedWorkStart)
            return chainWork.compareTo(other.chainWork) > 0;
        else return BlockIndexWorkComparator(this, other);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StoredBlock)) return false;
        StoredBlock o = (StoredBlock) other;
        return o.header.equals(header) && o.chainWork.equals(chainWork) && o.height == height;
    }

    @Override
    public int hashCode() {
        // A better hashCode is possible, but this works for now.
        return header.hashCode() ^ chainWork.hashCode() ^ height;
    }


    /**
     * Creates a new StoredBlock, calculating the additional fields by adding to the values in this block.
     */
    /*public StoredBlock build(Block block) throws VerificationException {
        // Stored blocks track total work done in this chain, because the canonical chain is the one that represents
        // the largest amount of work done not the tallest.
        BigInteger chainWork = this.chainWork.add(block.getWork());
        int height = this.height + 1;
        return new StoredBlock(block, chainWork, height);
    }*/
    public StoredBlock build(Block block, BlockStore blockStore) throws VerificationException {
        // Stored blocks track total work done in this chain, because the canonical chain is the one that represents
        // the largest amount of work done not the tallest.


        BigInteger chainWork = this.chainWork.add(/*block.getWork()*/getWorkAdjusted(blockStore));
        int height = this.height + 1;
        initAlgoWork(block, blockStore);
        return new StoredBlock(block, chainWork, height);
    }
    void initAlgoWork(Block block, BlockStore blockStore)
    {
        for (int i = 0; i < Block.NUM_ALGOS; i++)
        {
                nAlgoWork[i] = this.nAlgoWork[i].add(i == this.getHeader().getAlgo() ? block.getWork() : BigInteger.ZERO);
        }
    }

    /**
     * Given a block store, looks up the previous block in this chain. Convenience method for doing
     * <tt>store.get(this.getHeader().getPrevBlockHash())</tt>.
     *
     * @return the previous block in the chain or null if it was not found in the store.
     */
    public StoredBlock getPrev(BlockStore store) throws BlockStoreException {
        return store.get(getHeader().getPrevBlockHash());
    }

    /** Serializes the stored block to a custom packed format. Used by {@link CheckpointManager}. */
    public void serializeCompact(ByteBuffer buffer) {
        byte[] chainWorkBytes = getChainWork().toByteArray();
        boolean afterWorkStart2 = height > CoinDefinition.nBlockAlgoNormalisedWorkStart2;
        checkState(chainWorkBytes.length <= CHAIN_WORK_BYTES, "Ran out of space to store chain work!  " + chainWorkBytes.length +" bytes required");
        if (chainWorkBytes.length < CHAIN_WORK_BYTES) {
            // Pad to the right size.
            buffer.put(EMPTY_BYTES, 0, CHAIN_WORK_BYTES - chainWorkBytes.length);
        }
        buffer.put(chainWorkBytes);
        buffer.putInt(getHeight());
        // Using unsafeBitcoinSerialize here can give us direct access to the same bytes we read off the wire,
        // avoiding serialization round-trips.
        byte[] bytes = getHeader().unsafeBitcoinSerialize();
        buffer.put(bytes, 0, Block.HEADER_SIZE);  // Trim the trailing 00 byte (zero transactions).
    }

    /** De-serializes the stored block from a custom packed format. Used by {@link CheckpointManager}. */
    public static StoredBlock deserializeCompact(NetworkParameters params, ByteBuffer buffer) throws ProtocolException {
        byte[] chainWorkBytes = new byte[StoredBlock.CHAIN_WORK_BYTES];
        buffer.get(chainWorkBytes);
        BigInteger chainWork = new BigInteger(1, chainWorkBytes);
        int height = buffer.getInt();  // +4 bytes
        byte[] header = new byte[Block.HEADER_SIZE + 1];    // Extra byte for the 00 transactions length.
        buffer.get(header, 0, Block.HEADER_SIZE);
        return new StoredBlock(new Block(params, header), chainWork, height);
    }

    @Override
    public String toString() {
        return String.format("Block %s at height %d: %s",
                getHeader().getHashAsString(), getHeight(), getHeader().toString());
    }

    int getAlgoWorkFactor()
    {
        if (getHeader().params.getId().equals(NetworkParameters.ID_TESTNET) && (height < CoinDefinition.nBlockAlgoWorkWeightStart))
        {
            return 1;
        }
        if (getHeader().params.getId().equals(NetworkParameters.ID_TESTNET) && (height < 100))
        {
            return 1;
        }
        switch (getHeader().getAlgo())
        {
            case Block.ALGO_SHA256D:
                return 1;
            // work factor = absolute work ratio * optimisation factor
            case Block.ALGO_SCRYPT:
                return 1024 * 4;
            case Block.ALGO_GROESTL:
                return 64 * 8;
            case Block.ALGO_SKEIN:
                return 4 * 6;
            case Block.ALGO_QUBIT:
                return 128 * 8;
            default:
                return 1;
        }
    }
    BigInteger getPrevWorkForAlgo(int algo, BlockStore blockStore)
    {
        //BigInteger nWork;
        try {
            StoredBlock cursor = getPrev(blockStore);
            while (cursor != null)
            {
                if (cursor.getHeader().getAlgo() == algo)
                {
                    return cursor.getHeader().getWork();
                }
                cursor = cursor.getPrev(blockStore);
            }
        }
        catch(BlockStoreException x)
        {

        }
        return CoinDefinition.getProofOfWorkLimit(algo);
    }

    BigInteger getPrevWorkForAlgoWithDecay(int algo, BlockStore blockStore)
    {
        int nDistance = 0;

        try {
            StoredBlock cursor = getPrev(blockStore);
            while (cursor != null)
            {
                if(nDistance > 32)
                {
                    return BigInteger.ZERO;//CoinDefinition.getProofOfWorkLimit(algo);
                }
                if (cursor.getHeader().getAlgo() == algo)
                {
                    BigInteger work = cursor.getHeader().getWork();
                    work = work.multiply(BigInteger.valueOf(32 - nDistance));
                    work = work.divide(BigInteger.valueOf(32));
                    //if (work.compareTo(CoinDefinition.getProofOfWorkLimit(algo)) < 0)
                    //    work = CoinDefinition.getProofOfWorkLimit(algo);
                    return work;
                }
                cursor = cursor.getPrev(blockStore);
            }
        }
        catch(BlockStoreException x)
        {

        }
                return BigInteger.ZERO;
            }
    BigInteger GetPrevWorkForAlgoWithDecay3(int algo, BlockStore blockStore)
    {
        try {
            int nDistance = 0;
            //BigInteger nWork;
            StoredBlock pindex = this.getPrev(blockStore);
            while (pindex != null)
            {
                if (nDistance > 100)
                {
                    return BigInteger.ZERO;
                }
                if (pindex.getHeader().getAlgo() == algo)
                {
                    BigInteger nWork = pindex.getHeader().getWork();
                    nWork = nWork.multiply(BigInteger.valueOf(100 - nDistance));
                    nWork = nWork.divide(BigInteger.valueOf(100));
                    return nWork;
                }
                pindex = this.getPrev(blockStore);
                nDistance++;
            }
        }
        catch(BlockStoreException x)
        {

        }
        return BigInteger.ZERO;
    }
    public static double root(double num, double root)
    {
        return Math.pow(Math.E, Math.log(num)/root);
    }
    BigInteger getWorkAdjusted(BlockStore blockStore)
    {
        BigInteger bnRes;
        if ((getHeader().params.getId().equals(NetworkParameters.ID_TESTNET) && (height > 50)) ||
                (height >= CoinDefinition.GeoAvgWork_Start))
        {
            BigInteger nBlockWork = getHeader().getWork();
            int nAlgo = getHeader().getAlgo();
            for (int algo = 0; algo < Block.NUM_ALGOS; algo++)
            {
                if (algo != nAlgo)
                {
                    BigInteger nBlockWorkAlt = GetPrevWorkForAlgoWithDecay3(algo, blockStore);
                    if (nBlockWorkAlt.compareTo(BigInteger.ZERO) != 0)
                        nBlockWork = nBlockWork.multiply(nBlockWorkAlt);
                }
            }
            bnRes = nBlockWork;
            // Compute the geometric mean
            bnRes = BigDecimal.valueOf(root(bnRes.doubleValue(),Block.NUM_ALGOS)).toBigInteger();

            // Scale to roughly match the old work calculation
            bnRes = bnRes.shiftLeft(8);
        }
        else
        if ((getHeader().params.getId().equals(NetworkParameters.ID_TESTNET) && (height > 500)) ||
                (height >= CoinDefinition.nBlockAlgoNormalisedWorkStart))
        {
            // Adjusted block work is the sum of work of this block and the
            // most recent work of one block of each algo
            BigInteger nBlockWork = getHeader().getWork();
            int nAlgo = getHeader().getAlgo();
            for (int algo = 0; algo < Block.NUM_ALGOS; algo++)
            {
                if (algo != nAlgo)
                {
                    if (height >= CoinDefinition.nBlockAlgoNormalisedWorkStart2)
                        nBlockWork = nBlockWork.add(getPrevWorkForAlgoWithDecay(algo, blockStore));
                    else
                        nBlockWork = nBlockWork.add(getPrevWorkForAlgo(algo, blockStore));
                }
            }
            bnRes = nBlockWork.divide(BigInteger.valueOf(Block.NUM_ALGOS));
        }
        else
        {
            bnRes = getHeader().getWork().multiply(BigInteger.valueOf(getAlgoWorkFactor()));
        }
        return bnRes;
    }
}
