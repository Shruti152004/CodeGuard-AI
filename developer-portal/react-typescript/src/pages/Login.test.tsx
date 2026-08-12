import { render } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Login from './Login';

describe('Login Component', () => {
  test('renders login title and sign-in button', () => {
    const { getByText, getByRole } = render(
      <BrowserRouter>
        <Login />
      </BrowserRouter>
    );
    expect(getByText(/Sign in to review code and debt/i)).toBeInTheDocument();
    expect(getByRole('button', { name: /Sign In/i })).toBeInTheDocument();
  });
});
