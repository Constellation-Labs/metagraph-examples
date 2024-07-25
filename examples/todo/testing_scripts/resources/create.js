module.exports = () => {
  const now = new Date();
  const oneDayInMillis = 1 * 1.5 * 60 * 60 * 1000;

  return {
    CreateTask: {
      description: "This is a task description",
      dueDate: (now.getTime() + oneDayInMillis).toString(),
      optStatus: {
        type: "InProgress"
      }
    }
  };
};
