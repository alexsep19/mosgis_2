define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({

            name: 'voc_oktmo_grid',

            show: {
                toolbar: false,
                footer: false,
            },

            columns: [                
                {field: 'area_code', caption: 'Код района/города МО', size: 10},
                {field: 'settlement_code', caption: 'Код поселения МО', size: 10},
                {field: 'locality_code', caption: 'Код населенного пункта МО', size: 10},
                {field: 'section_code', caption: 'Код раздела', size: 10},
                {field: 'site_name', caption: 'Наименование территории', size: 10},
                {field: 'add_info', caption: 'Дополнительная информация', size: 10},
                {field: 'description', caption: 'Описание', size: 10},
                {field: 'appr_date', caption: 'Дата утверждения', size: 10, render: _dt},
                {field: 'adop_date', caption: 'Дата принятия', size: 10, render: _dt},
            ],
            
            url: '/mosgis/_rest/?type=voc_oktmo',

        }).refresh ();

    }

})