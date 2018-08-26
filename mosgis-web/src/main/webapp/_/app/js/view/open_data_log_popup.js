define ([], function () {
                
    return function (data, view) {
                
        $(view).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({
            
                name: 'user_own_options_form',
                
                record: {},

                fields : [],

            });

            $('.w2ui-form .the_table_container').w2regrid ({ 

                name   : 'user_own_options_grid', 

                show: {
                    toolbar: true,
                    footer: true,
                    toolbarSearch   : false,
                    toolbarInput    : false,
                    skipRecords: false,
                },           

                columns: [                
                    {field: 'no', caption: '№', size: 10
//                        , render: function (i) {return i.user_option.is_on ? 'Установлено' : ''}
                    },
                    {field: 'dt', caption: 'от', size: 18
                        , render: function (i) {return dt_dmy (i.dt.substr (0, 10))}
                    },
                    {field: 'dt_from', caption: 'Начало', size: 18
                        , render: function (i) {return i.dt_from.substr (0, 19)}
                    },
                    {field: 'rd', caption: '%', size: 8
                        , render: function (i) {return Math.floor (100 * i.rd / (i.sz || 1)) + '%'}
                    },
                    {field: 'dt_to_fact', caption: 'Окончание', size: 18
                        , render: function (i) {return (i.dt_to_fact || '').substr (0, 19)}
                    },
                ],

                url: '/mosgis/_rest/?type=open_data&part=log',

                onDblClick: null,

            })
            .refresh ()

            $('#grid_user_own_options_grid_check_all').hide ()

        });
                
    }

});