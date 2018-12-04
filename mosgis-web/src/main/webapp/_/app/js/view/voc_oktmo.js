define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({

            name: 'voc_oktmo_grid',

            show: {
                toolbar: true,
                toolbarSearch: true,
            },

            searches: [
                {field: 'code', caption: 'Код', type: 'text'},
            ],

            columns: [                
                {field: 'code', caption: 'Код', size: 10},
                {field: 'site_name', caption: 'Наименование территории', size: 50},
                {field: 'add_info', caption: 'Дополнительная информация', size: 10, hidden: 1},
                {field: 'description', caption: 'Описание', size: 10, hidden: 1},
                {field: 'appr_date', caption: 'Дата утверждения', size: 10, render: _dt, hidden: 1},
                {field: 'adop_date', caption: 'Дата принятия', size: 10, render: _dt, hidden: 1},
            ],
            
            url: '/mosgis/_rest/?type=voc_oktmo',

            onDblClick: function (e) {

                if ($_SESSION.delete ('voc_oktmo_popup.on')) {
                    $_SESSION.set ('voc_oktmo_popup.data', this.get (e.recid))
                    w2popup.close ()
                }

            }

        }).refresh ();

    }

})