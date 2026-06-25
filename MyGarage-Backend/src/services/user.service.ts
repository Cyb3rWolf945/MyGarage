import prisma from "../prisma";

/**
 * Updates user profile (name and garageName).
 */
export async function updateUserProfile(
  userId: string,
  name?: string,
  garageName?: string
): Promise<{ ok: boolean }> {
  const updates: any = {};
  
  if (name !== undefined && name.trim()) {
    updates.name = name.trim();
  }
  
  if (garageName !== undefined && garageName.trim()) {
    updates.garageName = garageName.trim();
  }

  if (Object.keys(updates).length === 0) {
    return { ok: true };
  }

  await prisma.user.update({
    where: { id: userId },
    data: updates,
  });

  return { ok: true };
}
