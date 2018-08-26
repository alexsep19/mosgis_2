define ([], function () {
                
    return function (data, view) {
        
        $(w2ui ['out_soap_layout'].el ('main')).empty ().w2regrid ({
            
            name: 'out_soap_export_nsi_item_grid',

            show: {
                toolbar: false,
                footer: false,
            },

            records: data.records,

            columns: [
                {field: 'id', caption: 'Календарный день отправки сообщений', size: 30, render: function (i) {return dt_dmy (i.id.substr (0, 10))}},
                {field: 'cnt', caption: 'Количество сообщений, подготовленных к отправке за день', size: 100, render: 'int'},
                {field: 'cnt_ok', caption: 'Количество успешно отправленных сообщений', size: 100, render: 'int'},
                {field: 'cnt_failed', caption: 'Количество сообщений, отправленных с ошибкой', size: 100, render: 'int', attr: 'data-ref=1'},
            ],
            
            onDblClick: null,            

            onClick: $_DO.open_err_popup_out_soap_export_nsi_item

        }).refresh ()

    }

})