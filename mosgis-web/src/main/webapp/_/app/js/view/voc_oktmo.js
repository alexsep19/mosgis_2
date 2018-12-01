define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({

            name: 'voc_oktmo_grid',

            show: {
                toolbar: true,
                footer: false,
            },

            columnGroups : [
                {span: 3, caption: 'Код ОКТМО'},
                {span: 5, caption: '', master: true},
            ],

            columns: [                
                {field: 'area_code', caption: '', size: 2},
                {field: 'settlement_code', caption: '', size: 2},
                {field: 'locality_code', caption: '', size: 2},
                {field: 'site_name', caption: 'Наименование территории', size: 50},
                {field: 'add_info', caption: 'Дополнительная информация', size: 10, hidden: 1},
                {field: 'description', caption: 'Описание', size: 10, hidden: 1},
                {field: 'appr_date', caption: 'Дата утверждения', size: 10, render: _dt, hidden: 1},
                {field: 'adop_date', caption: 'Дата принятия', size: 10, render: _dt, hidden: 1},
            ],
            
            url: '/mosgis/_rest/?type=voc_oktmo',

        }).refresh ();

    }

})