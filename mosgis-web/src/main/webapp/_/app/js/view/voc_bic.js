define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
darn (data)        
        $(w2ui ['vocs_layout'].el ('main')).w2regrid ({

            name: 'voc_bic_grid',

            multiSelect: false,

            show: {
                toolbar: true,
                toolbarSearch: true,
            },

            searches: [
                {field: 'code', caption: 'Код', type: 'text'},
            ],

            columns: [                
                {field: 'bic', caption: 'БИК', size: 9},
                {field: 'account', caption: 'Кор. счёт', size: 21},
                {field: 'namep', caption: 'Наименование', size: 30},
                {field: 'regn', caption: 'Рег. №', size: 10},
                {field: 'datein', caption: 'от', size: 10, render: _dt},
                {field: 'label_address', caption: 'Адрес', size: 30},
                {field: 'code_vc_nsi_237', caption: 'Регион', size: 30, voc: data.vc_nsi_237},
                
                
/*                

Регистрационный номер - RegN
Наименование - NameP - обязательное поле.
Дата регистрации Банком России - DateIn

                {field: 'site_name', caption: 'Наименование территории', size: 50},
                {field: 'add_info', caption: 'Дополнительная информация', size: 10, hidden: 1},
                {field: 'description', caption: 'Описание', size: 10, hidden: 1},
                {field: 'appr_date', caption: 'Дата утверждения', size: 10, render: _dt, hidden: 1},
                {field: 'adop_date', caption: 'Дата принятия', size: 10, render: _dt, hidden: 1},
*/                
            ],
            
            url: '/mosgis/_rest/?type=voc_bic',

            onDblClick: function (e) {
            }

        }).refresh ();

    }

})