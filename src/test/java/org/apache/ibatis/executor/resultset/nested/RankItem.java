package org.apache.ibatis.executor.resultset.nested;

public class RankItem {
    private Subject subject;
    private int score;

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

  @Override
  public String toString() {
    return "RankItem{" +
      "user=" + subject +
      ", score=" + score +
      '}';
  }
}
