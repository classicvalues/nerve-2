package network.nerve.pocbft.v1.entity;

import io.nuls.core.log.Log;
import network.nerve.pocbft.v1.utils.HashSetDuplicateProcessor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Eva
 */
public class VoteResultStageTwoQueue {

    /**
     * 投票结果去重
     */
    private HashSetDuplicateProcessor<String> duplicateProcessor = new HashSetDuplicateProcessor<>(1024);
    /**
     * 所有第二阶段的结果都提交到这里
     */
    private LinkedBlockingQueue<VoteStageResult> voteMessageQueue = new LinkedBlockingQueue<>();

    public void clear() {
        this.voteMessageQueue.clear();
    }

    public boolean offer(VoteStageResult result) {

        String key = result.getHeight() + "_" + result.getPackingIndexOfRound() + "_" + result.getRoundIndex() +
                "_" + result.getVoteRoundIndex() + "_" + result.getBlockHash().toHex();
        if (!duplicateProcessor.insertAndCheck(key)) {
            return false;
        }
        return this.voteMessageQueue.offer(result);
    }

    public VoteStageResult take() throws InterruptedException {
        return this.voteMessageQueue.take();
    }

    public VoteStageResult poll(long timeout, TimeUnit unit) {
        try {
            return this.voteMessageQueue.poll(timeout, unit);
        } catch (InterruptedException e) {
            Log.error(e);
        }
        return null;
    }

}
