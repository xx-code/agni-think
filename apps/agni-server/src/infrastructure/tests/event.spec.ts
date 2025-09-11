// pushNotificationUseCase.spec.ts
import { describe, it, expect, vi, beforeEach } from "vitest";
import { PushNotificationUseCase, RequestPushNotification } from "../../core/interactions/notification/pushNotification";
import Notification from "../../core/domains/entities/notification";
import { CreatedDto } from "../../core/dto/base";
import { EventRegister } from "../event";

// Mock repository
class FakeRepository<T> {
  public create = vi.fn<() => Promise<void>>(() => Promise.resolve());
}

// Mock GetUID (to control ID generation)
vi.mock("@core/adapters/libs", () => ({
  GetUID: () => "fixed-uid"
}));

describe("PushNotificationUseCase", () => {
  let repo: FakeRepository<Notification>;
  let usecase: PushNotificationUseCase;

  beforeEach(() => {
    repo = new FakeRepository<Notification>();
    usecase = new PushNotificationUseCase(repo as any);
  });

  it("should create a notification and return its id", async () => {
    const request: RequestPushNotification = {
      title: "Hello",
      content: "World",
      dateTime: new Date("2025-09-08T10:00:00Z")
    };

    const result: CreatedDto = await usecase.execute(request);

    expect(repo.create).toHaveBeenCalledTimes(1);
    expect(result).toEqual({ newId: "fixed-uid" });
  });

  it("should handle update() by calling execute()", async () => {
    const spy = vi.spyOn(usecase, "execute").mockResolvedValue({ newId: "fixed-uid" });

    usecase.update({
      title: "Event title",
      content: "Event content"
    });

    expect(spy).toHaveBeenCalledWith({
      title: "Event title",
      content: "Event content",
      dateTime: expect.any(Date)
    });
  });
});

describe("EventRegister", () => {
  let register: EventRegister;
  let listener: { update: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    register = new EventRegister();
    listener = { update: vi.fn() };
  });

  it("should subscribe and notify listeners", () => {
    register.subscribe("some-event", listener as any);

    register.notify("some-event", { title: "T", content: "C" });

    expect(listener.update).toHaveBeenCalledWith({ title: "T", content: "C" });
  });

  it("should unsubscribe listener", () => {
    register.subscribe("e1", listener as any);
    register.unsubscribe("e1", listener as any);

    register.notify("e1", { title: "X", content: "Y" });

    expect(listener.update).not.toHaveBeenCalled();
  });
});
