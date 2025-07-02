import React from 'react';

const AppsLogo: React.FC<{ width: number; height: number; color: string }> = (props) => {
  const { width, height, color } = props;
  return (
    <svg width={width} height={height} color={color} version="1.1" viewBox="0 0 180 180" xmlns="http://www.w3.org/2000/svg">
    <g>
     <path d="m63.572 30v21.428h21.428a38.571 38.571 0 0 1 38.572 38.572 38.571 38.571 0 0 1-38.572 38.572h-21.377-21.428v21.428h21.428 21.377 0.050781a60 60 0 0 0 59.949-60 60 60 0 0 0-60-60h-21.428z" fill={color} />
     <rect x="42.143" y="30" width="21.429" height="21.429" fill="#ff8c00"/>
     <path d="m42.143 51.428v21.43 21.428h21.43v-21.428-21.43h-21.43z" fill={color} />
    </g>
   </svg>

  );
};

export default AppsLogo;
