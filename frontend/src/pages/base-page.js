import {
    AppBar,
    Autocomplete,
    Avatar,
    Backdrop,
    Button,
    CircularProgress,
    IconButton,
    InputAdornment,
    Menu,
    MenuItem,
    TextField,
    Toolbar,
    Tooltip,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import React from "react";
import styles from "../components/HomeComponent/JobManagement.module.css";

class BasePage extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            searchBarText: "",
        };
    }

    searchAdornment = (otherAdornment) => {
        return (
            <>
                <InputAdornment position="start">
                    <IconButton style={{cursor: "inherit"}} disableRipple edge="end">
                        <SearchIcon/>
                    </IconButton>
                </InputAdornment>
                {otherAdornment?.map((adornment) => {
                    return adornment;
                })}
            </>
        );
    };

    render() {
        return (
            <>
                <AppBar className={"app-bar"} position="static" open variant="dense">
                    <Toolbar style={{display: "flex", justifyContent: "space-between", padding: "5px 0"}}>
                        <img
                            draggable={false}
                            src="HAMBlogo.png"
                            style={{cursor: "pointer", height: "50px"}}
                            className={"app-bar-logo"}
                            alt=""
                            onClick={() => {
                                this.props.navigate("/home");
                            }}
                        />

                        {/*<div style={{ margin: "0 50px" }}>*/}
                        {/*  <Button*/}
                        {/*    style={{ textTransform: "none" }}*/}
                        {/*    onClick={() => {*/}
                        {/*      this.props.navigate("/home");*/}
                        {/*    }}*/}
                        {/*    disableRipple*/}
                        {/*    variant="text"*/}
                        {/*    size="large"*/}
                        {/*  >*/}
                        {/*    Home Page*/}
                        {/*  </Button>*/}
                        {/*</div>*/}
                        {/*<div style={{ display: "flex", flexGrow: "1", margin: "1px 100px" }}>*/}
                        {/*  <Autocomplete*/}
                        {/*    multiple*/}
                        {/*    inputValue={this.state.searchBarText}*/}
                        {/*    value={[]}*/}
                        {/*    onChange={() => {}}*/}
                        {/*    freeSolo*/}
                        {/*    className="app-bar-search-bar"*/}
                        {/*    options={[]}*/}
                        {/*    defaultValue={[]}*/}
                        {/*    open={!this.props.isLoading}*/}
                        {/*    handleHomeEndKeys*/}
                        {/*    renderInput={(params) => {*/}
                        {/*      return (*/}
                        {/*        <TextField*/}
                        {/*          {...params}*/}
                        {/*          onChange={(e) => {*/}
                        {/*            this.setState({*/}
                        {/*              searchBarText: e.target.value,*/}
                        {/*            });*/}
                        {/*          }}*/}
                        {/*          size="small"*/}
                        {/*          label="Search Bar"*/}
                        {/*          InputProps={{*/}
                        {/*            ...params.InputProps,*/}
                        {/*            startAdornment: this.searchAdornment(*/}
                        {/*              params.InputProps.startAdornment*/}
                        {/*            ),*/}
                        {/*          }}*/}
                        {/*          inputProps={{*/}
                        {/*            ...params.inputProps,*/}
                        {/*          }}*/}
                        {/*        />*/}
                        {/*      );*/}
                        {/*    }}*/}
                        {/*  />*/}
                        {/*</div>*/}
                        {!this.props.isLoginPage &&
                        <div className={styles.barRight}>
                            <div className={styles.userDetails}>
                                <h1 className={styles.welcomeMessage}>Welcome, {this.props.username}</h1>
                                <text className={styles.currentDate}>{new Date().toDateString()}</text>
                            </div>
                            <div>
                                <img style={{width: 40, height: 40}}
                                     src="noticeicon.svg"
                                ></img>
                            </div>
                            <div>

                                <Tooltip title="Account settings">
                                    <Button
                                        style={{textTransform: "none"}}
                                        onClick={(e) => {
                                            this.setState({
                                                menuAnchorElement: e.currentTarget,
                                            });
                                        }}
                                        disableRipple
                                        variant="text"
                                        size="small"
                                    >

                                        <img
                                            style={{width: 40, height: 40}}
                                            src="imageicone.svg"
                                        ></img>
                                        {/*<span style={{margin: "0 8px"}}>Username</span>*/}
                                        <ExpandMoreIcon style={{marginRight: "8px", color: "gray"}}/>
                                    </Button>
                                </Tooltip>
                                <Menu
                                    anchorEl={this.state.menuAnchorElement}
                                    id="account-menu"
                                    open={!!this.state.menuAnchorElement}
                                    onClose={() => {
                                        this.setState({menuAnchorElement: null});
                                    }}
                                    onClick={() => {
                                        this.setState({menuAnchorElement: null});
                                    }}
                                    PaperProps={{
                                        elevation: 0,
                                        sx: {
                                            overflow: "visible",
                                            filter: "drop-shadow(0px 2px 8px rgba(0,0,0,0.32))",
                                            mt: 1.5,
                                            "& .MuiAvatar-root": {
                                                width: 32,
                                                height: 32,
                                                ml: -0.5,
                                                mr: 1,
                                            },
                                            "&:before": {
                                                content: '""',
                                                display: "block",
                                                position: "absolute",
                                                top: 0,
                                                right: 14,
                                                width: 10,
                                                height: 10,
                                                bgcolor: "background.paper",
                                                transform: "translateY(-50%) rotate(45deg)",
                                                zIndex: 0,
                                            },
                                        },
                                        style: {
                                            width: "220px",
                                        },
                                    }}
                                    transformOrigin={{horizontal: "right", vertical: "top"}}
                                    anchorOrigin={{horizontal: "right", vertical: "bottom"}}
                                >
                                    <MenuItem>...</MenuItem>
                                </Menu>
                            </div>
                        </div>}
                    </Toolbar>
                </AppBar>
                <div>
                    <Backdrop
                        sx={{
                            color: "#fff",
                            zIndex: (theme) => theme.zIndex.drawer + 1,
                        }}
                        open={this.props.isLoading}
                    >
                        <CircularProgress color="inherit"/>
                    </Backdrop>
                </div>
                {
                    this.props.component(this.props)
                }
            </>
        )
            ;
    }
}

export default BasePage;
