define ([], function () {

    return function (data, view) {
        
        var $panel
        
        var admin_layout = w2ui ['admin_layout']

        if (admin_layout) {            
            admin_layout.unlock ('main')
            $panel = $(admin_layout.el ('main'))
        }
        else {
            $panel = $('body')
            $('title').text ('Реестр объектов ГИС РД')            
        }
        
        $panel.w2regrid ({ 

            name: 'vc_rd_1_grid',             

            show: {
                toolbar: true,
                footer: true,
            },            

            toolbar: {
                items: [                
                    {type: 'button', id: 'printButton', caption: 'MS Excel', onClick: function () {this.owner.saveAsXLS ()}},
                    {type: 'button', id: 'maxButton', caption: 'Открыть в отдельной вкладке', onClick: function () {openTab ('/vc_rd_1')}, off: !admin_layout},
                ].filter (not_off),
                                
            },                      

            searches: [
                {field: 'id',            caption: 'objId',        type: 'int'},
                {field: 'unom',          caption: 'UNOM',         type: 'int'},
                {field: 'address_uc',    caption: 'Адрес',        type: 'text'},
                {field: 'id_vc_rd_1240', caption: 'Тип',          type: 'enum', options: {items: data.vc_rd_1240.items}},
                {field: 'id_vc_rd_1540', caption: 'Статус',       type: 'enum', options: {items: data.vc_rd_1540.items}},
            ],

            columns: [                
                {field: 'id', caption: 'objId',          size: 10, editable: {type: 'text'}},
                {field: 'unom', caption: 'UNOM',          size: 10, editable: {type: 'text'}},
                {field: 'address', caption: 'Адрес',    size: 100, editable: {type: 'text'}},
                {field: 'id_vc_rd_1240', caption: 'Тип',    size: 20, render: function (r) {return data.vc_rd_1240 [r.id_vc_rd_1240]}},
                {field: 'id_vc_rd_1540', caption: 'Статус', size: 20, render: function (r) {return data.vc_rd_1540 [r.id_vc_rd_1540]}},
                {field: 'ts_get_object', caption: 'Дата загрузки', size: 30, render: function (r) {return r.ts_get_object.substring (0, 19)}},
            ],

            url: '/mosgis/_rest/?type=vc_rd_1',
            
            onDblClick: null,

        }).refresh ();

        $('#grid_vc_rd_1_grid_search_all').focus ()

    }

})