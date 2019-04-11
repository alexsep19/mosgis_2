define ([], function () {

    return function (data, view) {
                
        var $main = $(w2ui ['administr_layout'].el ('main'))
                        
        $main.w2regrid ({
            
            name: 'tables_grid',
                        
            show: {
                toolbar: true,
                toolbarReload: false,
                footer: true,
            },
            
            searches: [            
                {field: 'recid',  caption: 'Имя',  type: 'text', operator: 'contains'},
                {field: 'label',  caption: 'Комментарий',  type: 'text', operator: 'contains'},
            ].filter (not_off),
            
            columns: [
                {field: 'recid', caption: 'Имя', size: 20, sortable: 1},
                {field: 'label', caption: 'Комментарий', size: 100, sortable: 1},
            ],
            
            records: data.tables,
            
            onDblClick: function (e) {
                openTab ('/table/' + e.recid)
            },                        
            
        }).refresh ()

    }

});