define ([], function () {

    return function (data, view) {

        var vocs_layout = w2ui ['vocs_layout']
                
        var $main = $(w2ui ['vocs_layout'].el ('main'))
                        
        $main.w2regrid ({
            
            name: 'nsi_grid',
                        
            show: {
                toolbar: true,
                toolbarReload: false,
                footer: true,
            },
            
            searches: [            
                {field: 'recid',  caption: 'Код',  type: 'text'},
                {field: 'label',  caption: 'Наименование',  type: 'text', operator: 'contains'},
            ].filter (not_off),
            
            columns: [
                {field: 'recid', caption: 'Код', size: 10, sortable: 1},
                {field: 'label', caption: 'Наименование', size: 100, sortable: 1},
            ],
            
            records: data.vc_nsi_list,
            
            onDblClick: function (e) {
                var s = w2ui ['sidebar']
                var id = e.recid
                var r = s.get (id)
                s.expand (r.parent.id)
                s.scrollIntoView (id)            
                s.click (id)            
            },            
            
        }).refresh ()

    }

});