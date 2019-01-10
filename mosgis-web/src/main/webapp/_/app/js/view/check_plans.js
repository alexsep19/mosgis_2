define ([], function () {

    return function (data, view) {
        
        $(w2ui ['supervision_layout'].el ('main')).w2regrid ({ 

            name: 'check_plans_grid',

            show: {
                toolbar: true,
                footer: true,
            },
            
            //searches: [
            //    {field: 'is_condo',            caption: 'Тип',           type: 'list', options: {items: [
            //        {id: "1", text: 'МКД'},
            //        {id: "0", text: 'ЖД'},
            //    ]}},
            //    {field: 'address_uc',           caption: 'Адрес',         type: 'text'},
            //    {field: 'fiashouseguid',           caption: 'GUID ФИАС',         type: 'text', operators: ['null', 'not null', 'is'], operator: 'is'},
            //],

            columns: [
                {field: 'year', caption: 'Год', size: 5},
                {field: 'uriregistrationplannumber', caption: 'Регистрационный номер плана в ЕРП', size: 10},
                {field: 'shouldnotberegistered', caption: 'Не должен быть зарегестрирован в ЕРП', size: 10, render: function (r) {
                    return r.shouldnotberegistered ? 'Да' : 'Нет'
                }},
                {field: 'sign', caption: 'Подписан', size: 10, render: function (r) {
                    return r.sign ? 'Да' : 'Нет'
                }}
            ],

            //url: '/mosgis/_rest/?type=check_plans',
            
            onDblClick: function (e) {
                //openTab ('/check_plan/' + e.recid)
            }

        }).refresh ();

        //$('#grid_check_plans_grid_search_all').focus ()

    }

})