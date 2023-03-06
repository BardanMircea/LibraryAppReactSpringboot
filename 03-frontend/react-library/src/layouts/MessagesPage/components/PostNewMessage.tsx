import { useOktaAuth } from "@okta/okta-react";
import { useState } from "react";
import MessageModel from "../../../models/MessageModel";

export const PostNewMessage = () => {
  const { authState } = useOktaAuth();
  const [title, setTitle] = useState("");
  const [question, setQuestion] = useState("");
  const [displayWarning, setDisplayWarning] = useState(false);
  const [displaySuccess, setDisplaySuccess] = useState(false);

  async function submitNewQuestion() {
    const url = `http://localhost:8001/api/messages/secure/add/message`;
    if (authState?.isAuthenticated && title !== "" && question !== "") {
      const messageRequestModel: MessageModel = new MessageModel(
        title,
        question
      );
      const requestOptions = {
        method: "POST",
        headers: {
          Authorization: `Bearer ${authState.accessToken?.accessToken}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(messageRequestModel),
      };
      const submintNewQuestionResponse = await fetch(url, requestOptions);
      if (!submintNewQuestionResponse.ok) {
        throw new Error("Something went wrong");
      }

      setTitle("");
      setQuestion("");
      setDisplayWarning(false);
      setDisplaySuccess(true);
    } else {
      setDisplayWarning(true);
      setDisplaySuccess(false);
    }
  }

  return (
    <div className="card mt-3">
      <div className="card-header">Ask a question to our admins</div>
      <div className="card-body">
        <form method="POST" action="">
          {displayWarning && (
            <div className="alert alert-danger" role="alert">
              All fields must be filled out
            </div>
          )}
          {displaySuccess && (
            <div className="alert alert-success" role="alert">
              Question added successfully
            </div>
          )}
          <div className="mb-3">
            <label className="form-label" htmlFor="">
              Title
            </label>
            <input
              type="text"
              className="form-control"
              id="exampleFormControlInput1"
              placeholder="Title"
              onChange={(e) => setTitle(e.target.value)}
              value={title}
            />
          </div>
          <div className="mb-3">
            <label className="form-label" htmlFor="">
              Question
            </label>
            <textarea
              className="form-control"
              id="exampleFormControlTextArea1"
              rows={3}
              onChange={(e) => setQuestion(e.target.value)}
              value={question}
            ></textarea>
          </div>
          <div>
            <button
              type="button"
              className="btn btn-primary mt-3"
              onClick={submitNewQuestion}
            >
              Submit Question
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
