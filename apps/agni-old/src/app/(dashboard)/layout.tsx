import { Box, Container } from '@mui/material';
import NavBar from '../components/navbar';
import TopNavBar from '../components/topNavbar';
import './page.css';

export default function DashboardLayout({
    children
  }: {
    children: React.ReactNode,
  }) {
    return (
       <Box sx={{display: 'flex', height: '100vh'}} >
          <NavBar />
          <Container>
            <TopNavBar />
            {children}
          </Container> 
        </Box> 
    ) 
  }