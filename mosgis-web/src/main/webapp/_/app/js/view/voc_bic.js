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
            
            toolbar: {

                items: [
                    {type: 'button', id: 'edit', caption: 'Обновить', onClick: $_DO.import_voc_bic, icon: 'w2ui-icon-pencil'},
                ],
            
            },

            searches: [
                {field: 'bic', caption: 'БИК', type: 'text'},
                {field: 'account', caption: '№ корреспондентского счёта', type: 'text'},
                {field: 'namep', caption: 'Наименование', type: 'text'},
                {field: 'regn', caption: 'Рег. №', type: 'text'},
                {field: 'datein', caption: 'от', type: 'text'},
                {field: 'code_vc_nsi_237', caption: 'Регион', type: 'enum', options: {items: data.vc_nsi_237.items}},
            ],

            columns: [                
                {field: 'bic', caption: 'БИК', size: 9},
                {field: 'account', caption: 'Кор. счёт', size: 21},
                {field: 'namep', caption: 'Наименование', size: 30},
                {field: 'regn', caption: 'Рег. №', size: 10},
                {field: 'datein', caption: 'от', size: 10, render: _dt},
                {field: 'label_address', caption: 'Адрес', size: 30},
                {field: 'code_vc_nsi_237', caption: 'Регион', size: 30, voc: data.vc_nsi_237},
            ],
            
            url: '/mosgis/_rest/?type=voc_bic',

            onDblClick: function (e) {
            }

        }).refresh ();

    }

})