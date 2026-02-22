/**
 * User Controller
 * Handles user-related API requests
 */

// Mock user database (in-memory)
const users = [
  { id: 123, name: 'Alice Smith', email: 'alice@example.com' },
  { id: 456, name: 'Bob Johnson', email: 'bob@example.com' },
  { id: 789, name: 'Charlie Brown', email: 'charlie@example.com' }
];

/**
 * Get single user by ID
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
async function getUserById(req, res) {
  const userIdRaw = req.params.id;

  // Normalize and validate path parameter: convert to number
  const userIdNum = Number(userIdRaw);
  if (!Number.isInteger(userIdNum)) {
    return res.status(400).json({ error: 'Invalid user id' });
  }

  const user = users.find(u => u.id === userIdNum);

  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }

  res.json(user);
}

/**
 * Get all users
 * @param {Object} req - Express request object
 * @param {Object} res - Express response object
 */
async function getAllUsers(req, res) {
  res.json(users);
}

module.exports = {
  getUserById,
  getAllUsers
};
