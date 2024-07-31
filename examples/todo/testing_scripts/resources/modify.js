module.exports = () => {
  const now = new Date();
  const twoDayInMillis = 2 * 24 * 60 * 60 * 1000;

  return {
    ModifyTask: {
      id: "e3e01c94b46dd1b67ef606f872648e47c31e1757ffd0a5ae0835e35cf8d53206",
      optStatus: { type: "InReview" },
      // optDueDate: (now.getTime() + twoDayInMillis).toString(),
    }
  };
};
